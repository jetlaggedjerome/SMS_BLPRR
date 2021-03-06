package com.sms.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sms.dao.LoginDAO;
import com.sms.entity.Login;
import com.sms.service.LoginService;
import com.sun.java.swing.plaf.windows.resources.windows;

public class LoginServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6077917519533177725L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String page = "";
		HttpSession session = request.getSession();
		String user = session.getAttribute("user") == null? "": (String) session.getAttribute("user");
		String action = request.getParameter("action") == null? "": request.getParameter("action") ;
		try {
			if(!"".equals(user) && "".equals(action)){
				page="home.jsp";
				response.setHeader("status", "success");
			} else{
			ApplicationContext applicationContext = 
					new ClassPathXmlApplicationContext("/com/sms/resource/applicationContext.xml");
		
			LoginService loginService = (LoginService) applicationContext.getBean("loginService");
		
			loginService.updateResetAllAttempts();
			session.invalidate();
			loginService.getLogin(request);
			page = "index.jsp";
			}
		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}finally{
			RequestDispatcher rd = request.getRequestDispatcher(page);
			rd.forward(request, response);
		}
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException,ServletException{
		
		HttpSession session = request.getSession();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String page = "";
		try {
			
			ApplicationContext applicationContext = 
					new ClassPathXmlApplicationContext("/com/sms/resource/applicationContext.xml");
		
			LoginService loginService = (LoginService) applicationContext.getBean("loginService");
			String message = loginService.getLogin(request);

			if(session.getAttribute("count_user").equals(1)){
				if(session.getAttribute("tag").equals("N") || session.getAttribute("tag").equals("n")){
					out.print("<h4>Unfortunately, you are not allowed to access the system."
							+ " Please contact the Administrator</h4>");
					session.invalidate();
				}else{
					if(message.equals("SUCCESS")){
						page = "home.jsp";
						loginService.updateResetAttempts(request);
						response.setStatus(308);
						response.setHeader("status", "success");
					}else{
						out.println("<h4> " + session.getAttribute("message_locked_invalid") + "</h4>");
						String action = request.getParameter("action") == null ? "" : request.getParameter("action");
						
						if(session.getAttribute("message2").equals("LOCKED")){
							if("updateRecord".equals(action)){
								loginService.updateLogin(request);
								loginService.updateResetAttempts(request);
								session.invalidate();
							}
						}else{
							loginService.updateAttempts(request);
						}
					}
				}
				
			}else{
				out.println("<h4>No user found. Please register first.</h4>");
			}
		
			

		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
		finally{
			RequestDispatcher rd = request.getRequestDispatcher(page);
			rd.include(request, response);
		}
	}
}
