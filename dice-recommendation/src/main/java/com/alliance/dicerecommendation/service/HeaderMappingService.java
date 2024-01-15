package com.alliance.dicerecommendation.service;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.exception.ServiceException;
import com.alliance.dicerecommendation.model.DataMismatchHistoryLog;
import com.alliance.dicerecommendation.model.HeaderMapping;
import com.alliance.dicerecommendation.repository.DataMismatchHistoryRepository;
import com.alliance.dicerecommendation.repository.HeaderMappingRepository;
import com.alliance.dicerecommendation.request.DataMismatchHistoryRequest;
import com.alliance.dicerecommendation.request.HeaderMappingRequest;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HeaderMappingService {
    
    @Autowired
    private HeaderMappingRepository headerMappingRepository;

    @Autowired
    private DataMismatchHistoryRepository dataMismatchHistoryRepository;

    public HeaderMapping createHeaderMapping(HeaderMappingRequest headerMappingRequest) throws ServiceException {
        try{
            if(!(headerMappingRequest==null)){
                log.info("start - createHeaderMapping - headerMappingRequest: {}", headerMappingRequest);
                Integer countMappingId = headerMappingRepository.getMaxHeaderMappingId();
                Integer headerMappingId = (countMappingId != null) ? countMappingId + 1 : 1; 

                HeaderMapping headerMapping = new HeaderMapping();
                headerMapping.setHeaderMappingId(headerMappingId);
                headerMapping.setHeaderName(headerMappingRequest.getHeaderName());
                headerMapping.setDescription(headerMappingRequest.getDescription());
                headerMapping.setRemark(headerMappingRequest.getRemark());
                headerMapping.setHeaderType(headerMappingRequest.getHeaderType());
                headerMapping.setHeaderNameMapping(headerMappingRequest.getHeaderNameMapping());
                return headerMappingRepository.save(headerMapping);
            } else {
                log.info("createHeaderMapping - headerMappingRequest is null");
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_CONFLICT,"Request is null");
            }
        } catch (Exception ex) {
            log.error("createHeaderMapping - Exception: {}", ex);
        }
        return null;
    }

    public Optional<HeaderMapping> getHeaderMapping(Integer headerMappingId) {
        return headerMappingRepository.getHeaderMappingById(headerMappingId);
    }

    public List<HeaderMapping> getNameHeaderMapping(List<Integer> headerMappingIdList ) {
        return headerMappingRepository.getNameHeaderMappingInId(headerMappingIdList);
    }

    public DataMismatchHistoryLog addDataMismatchHistoryLog(DataMismatchHistoryRequest dataMismatchHistoryRequest) throws ServiceException {
        try{
            if(!(dataMismatchHistoryRequest==null)){
                log.info("start - addDataMismatchHistoryLog > dataMismatchHistoryRequest: {}", dataMismatchHistoryRequest);
              
                DataMismatchHistoryLog dataMismatchHistoryLog = new DataMismatchHistoryLog();
                dataMismatchHistoryLog.setCampaignId(dataMismatchHistoryRequest.getCampaignId());
                dataMismatchHistoryLog.setUuidType(dataMismatchHistoryRequest.getUuidType());
                dataMismatchHistoryLog.setUuid(dataMismatchHistoryRequest.getUuid());
                dataMismatchHistoryLog.setUploadedFileId(dataMismatchHistoryRequest.getUploadedFileId());
                dataMismatchHistoryLog.setCampaignScheduleId(dataMismatchHistoryRequest.getCampaignScheduleId());
                dataMismatchHistoryLog.setHeaderMappingId(dataMismatchHistoryRequest.getHeaderMappingId());
                dataMismatchHistoryLog.setDataName(dataMismatchHistoryRequest.getDataName());
                dataMismatchHistoryLog.setOriginalDataValue(dataMismatchHistoryRequest.getOriginalDataValue());
                dataMismatchHistoryLog.setMatchingDataValue(dataMismatchHistoryRequest.getMatchingDataValue());
                dataMismatchHistoryLog.setIsSuccessProfileCheck(dataMismatchHistoryRequest.getIsSuccessProfileCheck());
                dataMismatchHistoryLog.setDescription(dataMismatchHistoryRequest.getDescription());
                dataMismatchHistoryLog.setRemark(dataMismatchHistoryRequest.getRemark());
                return dataMismatchHistoryRepository.save(dataMismatchHistoryLog);
            } else {
                log.info("addDataMismatchHistoryLog - dataMismatchHistoryRequest is null");
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_CONFLICT,"Request is null");
            }
        } catch (Exception ex) {
            log.error("addDataMismatchHistoryLog - Exception: {}", ex);
        }
        return null;
    }
}

