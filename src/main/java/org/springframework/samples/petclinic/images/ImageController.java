package org.springframework.samples.petclinic.images;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/images")
public class ImageController {

	@Value("${upload.dir:/tmp/petclinic/images}")
	private String uploadDir;

	@PostMapping
	public String uploadImage(@RequestParam("image") MultipartFile imageFile) {
		if (imageFile.isEmpty() || !isImage(imageFile)) {
			return "Invalid image file";
		}

		try {
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			Path filePath = uploadPath.resolve(imageFile.getOriginalFilename());
			imageFile.transferTo(filePath.toFile());
		}
		catch (IOException e) {
			e.printStackTrace();
			return "Error occurred while saving image";
		}

		return "Image uploaded successfully";
	}

	private boolean isImage(MultipartFile file) {
		String contentType = file.getContentType();
		return contentType != null && contentType.startsWith("image");
	}

	@GetMapping
	public @ResponseBody List<String> listImages() {
		try {
			Path uploadPath = Paths.get(uploadDir);
			if (Files.exists(uploadPath)) {
				return Files.walk(uploadPath, 1).filter(path -> !path.equals(uploadPath)).map(uploadPath::relativize)
						.map(Path::toString).collect(Collectors.toList());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

}
