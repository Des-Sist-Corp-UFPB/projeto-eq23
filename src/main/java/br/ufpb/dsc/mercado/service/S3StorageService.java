package br.ufpb.dsc.mercado.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

/**
 * Serviço responsável por interagir com o Object Storage (S3 / MinIO).
 * Usado para armazenar mídias como fotos de produtos e avatares de usuários.
 */
@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String publicEndpoint;

    public S3StorageService(
            S3Client s3Client,
            @Value("${aws.s3.endpoint}") String endpoint,
            @Value("${aws.s3.public-endpoint}") String publicEndpoint,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicEndpoint = publicEndpoint;

        // O Presigner precisa ser configurado com o endpoint público para que
        // a URL gerada seja acessível pelo navegador do usuário.
        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    /**
     * Gera uma URL temporária (Presigned URL) para DOWNLOAD de um arquivo.
     * O navegador usará essa URL para baixar a imagem diretamente do MinIO.
     *
     * @param objectKey Nome do arquivo no bucket (ex: "produtos/teclado.jpg")
     * @return URL pré-assinada válida por 60 minutos.
     */
    public String gerarUrlDownload(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Gera uma URL temporária (Presigned URL) para UPLOAD de um arquivo.
     * O frontend poderá fazer um PUT direto nessa URL com o binário da imagem,
     * economizando banda do servidor Spring Boot.
     *
     * @param objectKey Nome do arquivo no bucket
     * @return URL pré-assinada válida por 15 minutos para upload.
     */
    public String gerarUrlUpload(String objectKey, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
}
