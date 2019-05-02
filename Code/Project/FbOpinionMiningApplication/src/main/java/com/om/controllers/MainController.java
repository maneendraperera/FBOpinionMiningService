package com.om.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * This class handles security mappings of the application.
 * 
 * @author Maneendra
 *
 */
@Controller
public class MainController {

	private Logger logger = LoggerFactory.getLogger(MainController.class);

	@RequestMapping(value = { "/", "/home" }, method = RequestMethod.GET)
	public String home(Model model, HttpServletRequest request) {
		logger.info("Home: Incoming Request : " + request.getSession().getId());
		logger.info("------------- home -------------");
		return "home";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model) {

		logger.info(" ---------- login --------- ");

		if (error != null) {
			model.addAttribute("error", "Invalid username and password!");
		}

		if (logout != null) {
			model.addAttribute("msg", "You've been logged out successfully.");
		}

		return "login";
	}

}
