package web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;













import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import web.data.Photo;
import web.data.provider.FSPhotoManager;
import web.data.provider.PhotoManager;
import web.util.ResourceResolver;
import web.LaunchWeb;


@SuppressWarnings("unused")
@RestController
@Controller
public class WebController implements java.io.Serializable {

	@Autowired
	private PhotoManager photoManager;
	
	private static Logger logger = LoggerFactory.getLogger(WebController.class);
	
	// http://localhost:8080/example
	@RequestMapping(value = "/example", method = RequestMethod.GET)
	String healthCheck() {
		return "Lets check ";
	}
	
	/**
     * This is a simple example of how to use a data manager
     * to retrieve the data and return it as an HTTP response.
     * <p>
     * Note, when it returns from the Spring, it will be
     * automatically converted to JSON format.
     * <p>
     * Try it in your web browser:
     * 	http://localhost:8080/cs480/user/user101
     */
    @RequestMapping(value = "/cs480/user/{photoId}", method = RequestMethod.GET)
    Photo getPhoto(@PathVariable("photoId") String photoId) {
    	Photo photo = photoManager.getPhoto(photoId);
        return photo;
    }
    
    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    void getPhoto(
            @PathVariable("userId") String userId,
            @PathVariable("photoId") String photoId,
            HttpServletResponse response) {
    	long startTime = System.currentTimeMillis();
    	try {
    		logger.debug("Accessing the getPhoto method in the WebController class");
    		FSPhotoManager.getPhoto(userId, photoId, response);
    		long endTime = System.currentTimeMillis() - startTime;
    	} catch (IOException e) {
    		logger.error("IOException with using getPhoto");
    		logger.error("Exception: " + e);
    		e.printStackTrace();
    		long endTime = System.currentTimeMillis() - startTime;
    	}
    }
    
    /**
     * This is an example of sending an HTTP POST request to
     * update a user's information (or create the user if not
     * exists before).
     *
     * You can test this with a HTTP client by sending
     *  http://localhost:8080/cs480/user/user101
     *  	name=John major=CS
     *
     * Note, the URL will not work directly in browser, because
     * it is not a GET request. You need to use a tool such as
     * curl.
     *
     * @param id
     * @param name
     * @param owner
     * @return
     */
    @RequestMapping(value = "/cs480/user/{photoId}", method = RequestMethod.POST)
    Photo updatePhoto(
    		@PathVariable("photoId") String id,
    		@RequestParam("name") String name,
    		@RequestParam(value = "owner", required = false) String owner) {
    	Photo photo = new Photo();
    	photo.setId(id);
    	photo.setOwner(owner);
    	photo.setName(name);
    	photoManager.updatePhoto(photo);
    	return photo;
    }

    /**
     * This API deletes the user. It uses HTTP DELETE method.
     *
     * @param userId
     */
    @RequestMapping(value = "/cs480/user/{photoId}", method = RequestMethod.DELETE)
    void deletePhoto(
    		@PathVariable("photoId") String photoId) {
    	photoManager.deletePhoto(photoId);
    }

    /**
     * This API lists all the users in the current database.
     *
     * @return
     */
    @RequestMapping(value = "/cs480/users/list", method = RequestMethod.GET)
    List<Photo> listAllPhotos() {
    	return photoManager.listAllPhotos();
    }

    /*********** Web UI Test Utility **********/
    /**
     * This method provide a simple web UI for you to test the different
     * functionalities used in this web service.
     */
    @RequestMapping(value = "/cs480/home", method = RequestMethod.GET)
    ModelAndView getPhotoHomepage() {
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("photos", listAllPhotos());
        return modelAndView;
    }
    
    @RequestMapping(value="/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }
    
    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@RequestParam("name") String name, 
            @RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = 
                        new BufferedOutputStream(
                        		new FileOutputStream(
                        				new File("C:\\Users\\Brian\\workspace\\WebProject\\PhotoBucket\\" + name)));
                stream.write(bytes);
                stream.close();
                return "You successfully uploaded " + name + "!";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }
    
    @RequestMapping(value = "/{photoId}", method = RequestMethod.POST)
    void addPhoto(
            @PathVariable("userId") String userId,
            @RequestParam("photoFile") MultipartFile file,
            HttpServletResponse response) {
    	long startTime = System.currentTimeMillis();
    	logger.info("UserID: " + userId);
    	logger.debug("Accessing the WebController addPhoto method");
    	try {
			FSPhotoManager.addPhoto(userId, file);
			long endTime = System.currentTimeMillis() - startTime;
		} catch (Exception e) {
			e.printStackTrace();
			long endTime = System.currentTimeMillis() - startTime;
		} 
    }
    
    @RequestMapping(value = "/photos", method = RequestMethod.GET)
    List<String> listPhotos(@PathVariable("userId") String userId) {
    	return FSPhotoManager.listPhotos(userId);
    }
}