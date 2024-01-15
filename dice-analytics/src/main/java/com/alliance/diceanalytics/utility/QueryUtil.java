package com.alliance.diceanalytics.utility;

import com.alliance.diceanalytics.model.CampaignJourney;
import com.alliance.diceanalytics.model.QueryCondition;
import com.alliance.diceanalytics.request.BaseReportInfoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Slf4j
@Component
public class QueryUtil {

    @Autowired
    private CassandraTemplate queryTemplate;

    //Normal Count
    public Integer countByCampaignFilterCondition(QueryCondition conditions[], BaseReportInfoRequest request){
        String campaignNames [] = request.getCampaignName();
        Integer count = 0;


        for (String campaignName : campaignNames){
            List<Integer> campaignIds = getCampaignIdsByName(campaignName);
            for (Integer id : campaignIds) {
                request.setCampaignId(id);
                count += countByFilterCondition(conditions, request);
            }
        }
        return count;
    }

    public Integer countByFilterCondition(QueryCondition conditions[], BaseReportInfoRequest request){

        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.addAll(getDefaultCondition(request));

        for (int i=0; i < conditions.length; i++)
            criteriaList.add(buildQueryCriteria(conditions[i]));

        Query countQuery = Query.query(criteriaList).columns(Columns.from("CREATED_ON"));

        return  queryTemplate
                .select(countQuery.withAllowFiltering(), request.getTableType())
                .size();

    }

    //Count Unique
    public Integer countUniqueByCampaignFilterCondition(QueryCondition conditions[], BaseReportInfoRequest request){
        String campaignNames [] = request.getCampaignName();
        Integer count = 0;

        for (String campaignName : campaignNames){
            List<Integer> campaignIds = getCampaignIdsByName(campaignName);
            for (Integer id : campaignIds) {
                request.setCampaignId(id);
                count += countUniqueByFilterCondition(conditions, request);
            }
        }
        return count;
    }

    public Integer countUniqueByFilterCondition(QueryCondition conditions[], BaseReportInfoRequest request){

        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.addAll(getDefaultCondition(request));

        for (int i=0; i < conditions.length; i++)
            criteriaList.add(buildQueryCriteria(conditions[i]));

        //FILTER FOR NULL CIF VALUES
        criteriaList.add(buildQueryCriteria(
                new QueryCondition("cif_no","", CriteriaDefinition.Operators.GTE)
        ));

        Query uniqueQuery = Query.query(criteriaList);


        return queryTemplate
                .select(uniqueQuery.withAllowFiltering(), request.getTableType())
                .stream()
                .distinct()
                .collect(toList())
                .size();

    }

    //Count OR
    public Integer countByORFilterCondition(QueryCondition orConditions[],
                                            QueryCondition andConditions[],
                                            BaseReportInfoRequest request){
        int count=0;


        for (int i =0; i < orConditions.length; i ++){
            List<QueryCondition> andConditionList = new ArrayList<>(Arrays.asList(andConditions));
            andConditionList.add(orConditions[i]);

            count+= countByFilterCondition(andConditionList.toArray(new QueryCondition[andConditionList.size()]),request);
        }

        return count;
    }


    public List<?> selectDistinctAllByConditions(QueryCondition conditions[], BaseReportInfoRequest request){
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.addAll(getDefaultCondition(request));

        for (int i=0; i < conditions.length; i++)
            criteriaList.add(buildQueryCriteria(conditions[i]));

        Query selectQuery = Query.query(criteriaList);

        List<?> result=queryTemplate
                .select(selectQuery.withAllowFiltering(), request.getTableType())
                .stream().distinct()
                .collect(Collectors.toList());

        return result;

    }

    public List<?> selectAllByConditions(QueryCondition conditions[], BaseReportInfoRequest request){
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.addAll(getDefaultCondition(request));

        for (int i=0; i < conditions.length; i++)
            criteriaList.add(buildQueryCriteria(conditions[i]));

        Query selectQuery = Query.query(criteriaList);

        List<?> result=queryTemplate.select(selectQuery.withAllowFiltering(), request.getTableType());
        return result;

    }

    public List<?> selectColumnsByConditions(QueryCondition conditions[],
                                             BaseReportInfoRequest request,
                                             String columnNames){
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.addAll(getDefaultCondition(request));

        for (int i=0; i < conditions.length; i++)
            criteriaList.add(buildQueryCriteria(conditions[i]));

        Query selectQuery = Query.query(criteriaList).columns(Columns.from(columnNames));

        List<?> result=queryTemplate
                .select(selectQuery.withAllowFiltering(), request.getTableType());

        return result;
    }

    //Supporting function
    public List<Criteria> getDefaultCondition(BaseReportInfoRequest request){

        List<Criteria> queryConditions= new ArrayList<>();

        if (request.getStartDate()!= null)
            queryConditions.add(
                    buildQueryCriteria(new QueryCondition("created_on",request.getStartDate(),CriteriaDefinition.Operators.GTE))
            );
        if(request.getEndDate()!= null)
            queryConditions.add(
                    buildQueryCriteria(new QueryCondition("created_on",request.getEndDate(),CriteriaDefinition.Operators.LTE))
            );


        if (request.getCampaignId()!= null){
            if (request.getTableType()== CampaignJourney.class)
                queryConditions.add(
                        buildQueryCriteria(new QueryCondition("campaign_id",request.getCampaignId().toString(),CriteriaDefinition.Operators.EQ))
                );
            else
                queryConditions.add(
                        buildQueryCriteria(new QueryCondition("campaign_id",request.getCampaignId(),CriteriaDefinition.Operators.EQ))
                );
        }

        return queryConditions;
    }

    public List<Integer> getCampaignIdsByName(String name){
        return  queryTemplate.select(
                "SELECT campaignid from evt_campaign_list WHERE campaignname ='{name}' ALLOW FILTERING".replace("{name}",name),
                Integer.class
        );

    }

    public Date getCampaignDateByName (String name){
        return  queryTemplate.select(
                "SELECT startdate from evt_campaign_list WHERE campaignname ='{name}' ALLOW FILTERING".replace("{name}",name),
                Date.class
        ).get(0);

    }

    public Criteria buildQueryCriteria(QueryCondition condition){
        Criteria queryCriteria;

        CriteriaDefinition.Predicate filterValue=
                new CriteriaDefinition.Predicate(condition.getFieldComparator(),condition.getFieldValue());

        queryCriteria =  Criteria.of(
                ColumnName.from(condition.getFieldName()),
                filterValue
        );

        return  queryCriteria;
    }



}
