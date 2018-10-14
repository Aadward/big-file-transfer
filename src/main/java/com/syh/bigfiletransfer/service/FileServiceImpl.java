package com.syh.bigfiletransfer.service;

import com.google.common.io.ByteStreams;
import com.syh.bigfiletransfer.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.repository}")
    private String repositoryPath;

    @PostConstruct
    public void init() {
        // Ensure the directory exists
        File repository = new File(repositoryPath);
        if (repository.exists() && repository.isFile()) {
            throw new RuntimeException("Can not create repository directory:" + repositoryPath);
        }

        if (!repository.exists() && !repository.mkdirs()) {
            throw new RuntimeException("Create repository directory failed:" + repositoryPath);
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadFullFile(String filename) throws FileNotFoundException {
        File file = getFile(filename).orElseThrow(() -> new FileNotFoundException(filename));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));


        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(new InputStreamResource(new FileInputStream(file)));
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadRangeFile(String filename, List<HttpRange> ranges)
            throws IOException {
        File file = getFile(filename).orElseThrow(() -> new FileNotFoundException(filename));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment");

        if (ranges.size() == 1) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(file.length());
            long end = range.getRangeEnd(file.length());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(end - start) + 1);
            headers.add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, file.length()));

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(new InputStreamResource(FileUtil.getFileStream(file, start, end)));
        } else {
            throw new RuntimeException("Not support multiple ranges");
        }
    }

    @Override
    public void uploadFile(String filename, InputStream in) throws IOException {
        File file = new File(fullName(filename));
        ByteStreams.copy(in, new FileOutputStream(file));
    }

    private Optional<File> getFile(String filename) {
        File file = new File(fullName(filename));
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    private String fullName(String filename) {
        return repositoryPath + File.separator + filename;
    }
}
