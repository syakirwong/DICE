package com.alliance.dicerecommendation.service;

import com.alliance.dicerecommendation.model.CampaignSchedule;
import com.alliance.dicerecommendation.model.DataField;
import com.alliance.dicerecommendation.model.HeaderMapping;
import com.alliance.dicerecommendation.repository.CampaignScheduleRepository;
import com.alliance.dicerecommendation.request.HeaderMappingRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tukaani.xz.CorruptedInputException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileService {

    @Autowired
    private CampaignScheduleRepository campaignScheduleRepository;

    @Autowired
    private HeaderMappingService headerMappingService;

    @Value("${file.name.prefix}")
    private String FILE_NAME_PREFIX;

    // public void read7zFile(CampaignSchedule campaignSchedule, String filePassword){
    //     log.info("start - read7zFile - campaignScheduleId : {} | filePath : {}", campaignSchedule.getCampaignScheduleId(), campaignSchedule.getFilePath()+"/"+campaignSchedule.getFileName());

    //     boolean foundFile = false;
    //     log.info(filePassword);
    //     try (SevenZFile sevenZFile = new SevenZFile(new File(campaignSchedule.getFilePath(), campaignSchedule.getFileName()), filePassword.toCharArray())) {
    //         // SevenZArchiveEntry archiveEntry = sevenZFile.getNextEntry();
    //         for(SevenZArchiveEntry entry : sevenZFile.getEntries()) {
    //             log.info("read7zFile - finding target file : {} for campaign schedule id : {}", campaignSchedule.getFileName(),campaignSchedule.getCampaignScheduleId());
    //             if(!entry.isDirectory() && entry.getName().startsWith("DICE_")){
    //                 foundFile = true;
    //                 log.info("read7zFile - found, directory : {}, fileName : {}", campaignSchedule.getFilePath(), entry.getName());
    //                 String fileExtension = FilenameUtils.getExtension(entry.getName());
    //                 if("csv".equals(fileExtension)){
    //                     log.info("read7zFile - fileName {} with csv extension", entry.getName());
    //                     // byte[] content = new byte[(int) entry.getSize()];
    //                     byte[] content = new byte[(int) entry.getSize()];
    //                      log.info("test1");
    //                     int bytesRead = sevenZFile.read(content, 0, content.length);
    //                    if (bytesRead !=-1){
    //                     log.info("process");
    //                    }
    //                    else{
    //                     log.error("failed");
    //                    }

                       
    //                     // int bytesRead = sevenZFile.read(content,0,content.length);
    //                     // log.info("test2");
    //                     // String fileContent = new String(content, StandardCharsets.UTF_8);
    //                     // log.info("test3");

    //                     // String[] fileContentArray = fileContent.split("\\r?\\n");
    //                     // handle for csv files
    //                     // processCsvFile(campaignSchedule,entry.getName(),fileContentArray);
    //                 }

    //                 if ("xslx".equals(fileExtension)){
    //                     // handle for excel files
    //                 }

    //                 break; // If file is found it will exit the loop
    //             }
    //         }

    //         if(!foundFile){
    //             // send email
    //             log.warn("File {} not found in the 7z archive",campaignSchedule.getFileName());
    //             campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
    //             campaignSchedule.setRemark("File not found in 7z archive");
    //         }

    //     } catch (NoSuchFileException e) {
    //         log.error("No file found at the specified path. - NoSuchFileException");
    //         campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
    //         campaignSchedule.setRemark("7z file not found in the path");
    //     }  catch (IOException e) {
    //         log.error("Error while accessing the 7z file: {}", e.getMessage());
    //         if (e.getMessage() != null && e.getMessage().startsWith("Checksum verification failed")) {
    //             campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
    //             campaignSchedule.setRemark("Wrong password");
    //         } else {
    //             campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
    //             campaignSchedule.setRemark(e.getMessage());
    //             e.printStackTrace();
    //         }
    //     } catch (Exception e) {
    //         log.error("Unexpected error: {}", e.getMessage());
    //         campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
    //         campaignSchedule.setRemark("Unexpected error occured: " + e.getMessage());
    //         e.printStackTrace();
    //     }

    // }

    public void read7zFile(CampaignSchedule campaignSchedule, String filePassword) {
        log.info("start - read7zFile - campaignScheduleId : {} | filePath : {} | fileName : {}", campaignSchedule.getCampaignScheduleId(), campaignSchedule.getFilePath(), campaignSchedule.getFileName());
        
        // TODO : get header mapping ids from campaign properties
        List<Integer> headerMappingIds = Arrays.asList(1);

        List<HeaderMapping> headerMappingList = headerMappingService.getNameHeaderMapping(headerMappingIds);

        List<HeaderMappingRequest> headerMappingRequestList = new ArrayList<>();

        for (HeaderMapping headerMapping : headerMappingList) {
            String headerName = headerMapping.getHeaderName();
            List<String> headerNameMapping = headerMapping.getHeaderNameMapping();
            String headerType = headerMapping.getHeaderType();
        
            HeaderMappingRequest headerMappingRequest = new HeaderMappingRequest(headerName, headerNameMapping, headerType);
            headerMappingRequestList.add(headerMappingRequest);
        }

        int fileCount = 0;
        List<Integer> columnIndexList = new ArrayList<>(); // List to store selected column indices
        List<String> missingHeaders = new ArrayList<>(); // List to track missing headers
        boolean foundFile = false; // Track if the file is found

        try (SevenZFile sevenZFile = new SevenZFile(new File(campaignSchedule.getFilePath(), campaignSchedule.getFileName()), filePassword.toCharArray())) {
            log.info("read7zFile - Successfully opened the 7z file - campaignScheduleId : {}", campaignSchedule.getCampaignScheduleId());

            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            // for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
            while (entry != null) {
                fileCount++;
                if (!entry.isDirectory() && entry.getName().startsWith(FILE_NAME_PREFIX)) {
                    log.info("read7zFile - found name start with {}, directory : {}, fileName : {}, campaignScheduleId : {}", FILE_NAME_PREFIX, campaignSchedule.getFilePath(), entry.getName(), campaignSchedule.getCampaignScheduleId());
                    String fileName = entry.getName();
                    String fileExtension = FilenameUtils.getExtension(fileName);

                    if ("csv".equalsIgnoreCase(fileExtension)) {
                        foundFile = true; // Set the flag indicating the file is found
                        log.info("read7zFile - fileName {} with csv extension , campaignScheduleId : {}", entry.getName(), campaignSchedule.getCampaignScheduleId());
                        byte[] content = new byte[(int) entry.getSize()];
                        // start to read the content
                        int bytesRead = sevenZFile.read(content, 0, content.length);
                        String fileContent = new String(content, "UTF-8");
                        String[] fileContentArray = fileContent.split("\\r?\\n");
                        // log.info("fileContentArray : {}",fileContentArray[1]);

                        // Process header row to find selected column indices
                        Map<String, Integer> headerColumnPair = new HashMap<>();
                        if (fileContentArray.length > 0) {
                            String[] headers = fileContentArray[0].split(",");
                            for (HeaderMappingRequest mappingRequest : headerMappingRequestList) {
                                boolean columnFound = false; // Flag to track if column is found
                                for (int i = 0; i < headers.length; i++) {
                                    // log.info("show header {} : {}", i, headers[i].trim());
                                    for (String headerMapping : mappingRequest.getHeaderNameMapping()) {
                                        if (headerMapping.equalsIgnoreCase(headers[i].trim())) {
                                            columnIndexList.add(i);
                                            columnFound = true;
                                            // Store header name and column index
                                            String headerName = mappingRequest.getHeaderName();
                                            
                                            headerColumnPair.put(headerName, i);
                                            // headerColumnIndices.add(headerColumnPair);
                                            break;
                                        }
                                    }
                                }

                                if (!columnFound) {
                                    missingHeaders.add(mappingRequest.getHeaderName()); // Track missing headers
                                }
                            }
                        }
                        log.info("read7zFile - headerColumnPair : {}", headerColumnPair);

                        if (missingHeaders != null){
                            // for (String row : fileContentArray){
                            //     log.info(row);
                            // }
                            processCsvFile(campaignSchedule, fileContentArray, headerColumnPair, columnIndexList);
                        }
                    
                        break;
                    }
                    
                }
                entry = sevenZFile.getNextEntry();
            }

            log.info("read7zFile - Total files processed until file found in 7z archive: {} | campaignScheduleId : {}", fileCount, campaignSchedule.getCampaignScheduleId());

            // if (!missingHeaders.isEmpty() && foundFile) {
            //     log.warn("The following headers were not found in the CSV file: {}", missingHeaders);
            // }

            if (!foundFile) {
                // send email
                log.warn("read7zFile - File {} not found in the 7z archive, ", campaignSchedule.getFileName());
                campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
                campaignSchedule.setRemark("File not found in 7z archive");
            } else {
                if (missingHeaders != null && !(missingHeaders.isEmpty())) {
                    log.warn("read7zFile - missingHeaders : {}", missingHeaders);
                    campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.INVALID);
                    campaignSchedule.setRemark("Missing header in 7z archive : " + String.join(", ", missingHeaders));
                }
            }


        } catch (NoSuchFileException e) {
            log.error("read7zFile - No file found at the specified path. -  NoSuchFileException : {} | campaignScheduleId : {}", e , campaignSchedule.getCampaignScheduleId());
            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
            campaignSchedule.setRemark("7z file not found in the path");
        } catch(CorruptedInputException e) {
            log.error("read7zFile - CorruptedInputException : {}", e.getMessage());
            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
            campaignSchedule.setRemark("Encounter CorruptedInputException: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("read7zFile -  ArrayIndexOutOfBoundsException : {}", e.getMessage());
            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
            campaignSchedule.setRemark("Encounter ArrayIndexOutOfBoundsException: " + e.getMessage());
        } catch (IOException e) {
            log.error("read7zFile : IOException : {}", e.getMessage());
            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
            campaignSchedule.setRemark("Encounter IOException: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
            campaignSchedule.setRemark("Unexpected error occured: " + e.getMessage());
        }
    }

    // public void processCsvFile(CampaignSchedule campaignSchedule, String fileName, String[] fileContentArray) throws IOException {
    //     List<String> selectedColumns = Arrays.asList("CIF No.", "Name", "Phone number", "Device Id", "Device Platform");

    //     List<Integer> columnIndexList = new ArrayList<>();
    //     List<String> missingHeaders = new ArrayList<>();
    //     if (fileContentArray.length > 0) {

    //         // Read and process header
    //         String[] headers = fileContentArray[0].split(",");
    //         for (String selectedColumn : selectedColumns) {
    //             boolean columnFound = false; // Flag to track if column is found
    //             for (int i = 0; i < headers.length; i++) {
    //                 if (selectedColumn.equalsIgnoreCase(headers[i].trim())) {
    //                     columnIndexList.add(i);
    //                     columnFound = true;
    //                     break;
    //                 }
    //             }
    //             if (!columnFound) {
    //                 missingHeaders.add(selectedColumn); // Track missing headers
    //                 throw new IOException("Header "+selectedColumn+" not found");
    //             }
    //         }


    //         if(campaignSchedule.getProcessedIndex() == null) campaignSchedule.setProcessedIndex(0);

    //         // Read and process data
    //         for (int i = campaignSchedule.getProcessedIndex()+1; i < fileContentArray.length; i++) {

    //             String[] values = fileContentArray[i].split(",");
    //             StringBuilder logMessage = new StringBuilder();
    //             for (Integer index : columnIndexList) {
    //                 if (values.length > index) {
    //                     logMessage.append(selectedColumns.get(index)).append(": ").append(values[index].trim()).append(", ");
    //                 }
    //             }
    //             if (logMessage.length() > 0) {
    //                 log.info(logMessage.substring(0, logMessage.length() - 2)); // Remove trailing comma and space
    //             }

    //             campaignSchedule.setProcessedIndex(i);
    //             campaignScheduleRepository.save(campaignSchedule); // to track until which index it has processed
    //         }

    //         campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.COMPLETED);
    //         campaignSchedule.setRemark("File processing is completed");

    //     } else {
    //         log.warn("File {} is empty", fileName);
    //         campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
    //         campaignSchedule.setRemark("File " + fileName + " is empty");
    //     }
    // }

    public void processCsvFile(CampaignSchedule campaignSchedule, String[] fileContentArray, Map<String, Integer> headerColumnPair, List<Integer> columnIndexList) throws IOException {
        if (fileContentArray.length > 0) {
            log.info("start - processCsvFile");
            if(campaignSchedule.getProcessedIndex() == null) campaignSchedule.setProcessedIndex(0);

            // Read and process data
            for (int i = campaignSchedule.getProcessedIndex()+1; i < fileContentArray.length; i++) {

                String[] values = fileContentArray[i].split(",");
                StringBuilder logMessage = new StringBuilder();
                for (Integer index : columnIndexList) {
                    if (values.length > index) {
                        // Retrieve the key from headerColumnPair based on columnIndexList
                        String key = getKeyFromValue(headerColumnPair, index);
                        log.info("processCsvFile key : {}", key);
                        if (key != null) {
                            logMessage.append(key).append(": ").append(values[index].trim()).append(", ");
                        }
                    }
                }
                if (logMessage.length() > 0) {
                    log.info(logMessage.substring(0, logMessage.length() - 2)); // Remove trailing comma and space
                }

                campaignSchedule.setProcessedIndex(i);
                campaignScheduleRepository.save(campaignSchedule); // to track until which index it has processed
            }

            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.COMPLETED);
            campaignSchedule.setRemark("File processing is completed");

        } else {
            log.warn("File {} is empty", campaignSchedule.getCampaignScheduleId());
            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.FAILED);
            campaignSchedule.setRemark("File " + campaignSchedule.getCampaignScheduleId() + " is empty");
        }
    }

    // Function to get the key from value in the map
    private String getKeyFromValue(Map<String, Integer> map, Integer value) {
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue().equals(value)) {
            return entry.getKey();
        }
    }
    return null; // If no key is found for the given value
}

}
