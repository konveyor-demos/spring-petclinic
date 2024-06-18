package org.springframework.samples.petclinic.images;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Testcontainers
public class ImageControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ImageController imageController;

	private S3Client s3Client;

	@Container
	LocalStackContainer s3 = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
			.withServices(LocalStackContainer.Service.S3);

	@BeforeEach
	public void setUp() {
		String endpoint = "http://" + s3.getHost() + ":" + s3.getMappedPort(4566);
		S3Configuration s3Configuration = S3Configuration.builder().pathStyleAccessEnabled(true).build();
		s3Client = S3Client.builder().endpointOverride(URI.create(endpoint)).region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
				.serviceConfiguration(s3Configuration).build();

		// Inject the S3Client into the ImageController
		ReflectionTestUtils.setField(imageController, "s3", s3Client);
	}

	@Test
	public void testUploadImage() throws Exception {
		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes());
		mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file)).andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testListImages() throws Exception {
		List<String> images = Arrays.asList("image1.jpg", "image2.jpg");

		// Prepare the S3 bucket and objects
		String bucketName = "images";
		s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
		images.stream().forEach(i -> s3Client.putObject(b -> b.bucket(bucketName).key(i),
				RequestBody.fromString(String.format("%s content", i))));

		// Perform the request
		mockMvc.perform(MockMvcRequestBuilders.get("/images")).andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(new ObjectMapper().writeValueAsString(images)));
	}

}
