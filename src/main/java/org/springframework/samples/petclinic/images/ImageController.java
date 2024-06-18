package org.springframework.samples.petclinic.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);

	private final S3Client s3;

	@Value("${upload.bucket:images}")
	private String bucketName;

	public ImageController(S3Client s3) {
		this.s3 = s3;
	}

	private void createBucketIfNotExists() {
		List<Bucket> buckets = s3.listBuckets().buckets();
		boolean bucketExists = buckets.stream().anyMatch(b -> b.name().equals(bucketName));

		if (!bucketExists) {
			s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
		}
	}

	@PostMapping
	public String uploadImage(@RequestParam("image") MultipartFile imageFile) {
		if (imageFile.isEmpty() || !isImage(imageFile)) {
			LOGGER.error("Invalid image file");
			return "Invalid image file";
		}

		try {
			createBucketIfNotExists();
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName)
					.key(imageFile.getOriginalFilename()).build();
			s3.putObject(putObjectRequest,
					RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));
		}
		catch (Exception e) {
			LOGGER.error("Error occurred while saving image", e);
			return "Error occurred while saving image";
		}

		LOGGER.debug("Image '{}' (size {}) uploaded successfully", imageFile.getOriginalFilename(),
				imageFile.getSize());
		return "Image uploaded successfully";
	}

	private boolean isImage(MultipartFile file) {
		String contentType = file.getContentType();
		return contentType != null && contentType.startsWith("image");
	}

	@GetMapping
	public @ResponseBody List<String> listImages() {
		createBucketIfNotExists();
		ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder().bucket(bucketName).build();
		ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);
		List<String> imageNames = new ArrayList<>();

		for (S3Object content : listObjResponse.contents()) {
			imageNames.add(content.key());
		}

		return imageNames;
	}

}
