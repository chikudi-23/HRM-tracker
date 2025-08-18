package com.hrmtracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String jobId;
    private String tempFilePath;
    private String originalFilename;
    private String fileType; // "idProof" or "resume"
    private String userEmail;
}
