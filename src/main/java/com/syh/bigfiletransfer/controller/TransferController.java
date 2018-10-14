package com.syh.bigfiletransfer.controller;

import com.google.common.base.Strings;
import com.syh.bigfiletransfer.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;

@RestController
@Slf4j
public class TransferController {

    @Autowired
    private FileService fileService;

    @PutMapping("/{filename}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upload(@PathVariable("filename") final String filename,
                       HttpServletRequest request) throws IOException {
        fileService.uploadFile(filename, request.getInputStream());
    }

    @GetMapping(value = "/{filename}", produces = "application/octet-stream")
    public ResponseEntity<InputStreamResource> download(@PathVariable("filename") final String filename,
                                      @RequestHeader(value = "range", required = false) String rangeStr)
            throws IOException {

        log.debug("request in, range=: {}", rangeStr);

        if (Strings.isNullOrEmpty(rangeStr)) {
            return fileService.downloadFullFile(filename);
        } else {
            List<HttpRange> ranges = HttpRange.parseRanges(rangeStr);
            return fileService.downloadRangeFile(filename, ranges);
        }
    }
}
