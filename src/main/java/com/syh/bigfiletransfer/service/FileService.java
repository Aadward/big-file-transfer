package com.syh.bigfiletransfer.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService {

    ResponseEntity<InputStreamResource> downloadFullFile(String filename) throws FileNotFoundException;

    ResponseEntity<InputStreamResource> downloadRangeFile(String filename, List<HttpRange> ranges) throws IOException;

    void uploadFile(String filename, InputStream in) throws IOException;
}
