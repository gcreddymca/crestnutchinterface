package com.hm.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.nutch.tools.JDBCConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.vo.UserVO;
import com.hm.util.PasswordUtil;

/**
 * This class contains user login, edit, delete operations
 */
public class LoginAuthenticateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(LoginAuthenticateServlet.class);
	
	private ResourceBundle bundle = null;   
    
    public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestType = request.getParameter("requestType");
		int roleId=0;
		String roleType=null;
		if(requestType!=null && !requestType.isEmpty()){
		if (requestType.equalsIgnoreCase("logIn")) {
			String userName = request.getParameter("username");
			String password = request.getParameter("password");
			if(userName!=null && password!=null && !userName.isEmpty() && !password.isEmpty()){
				Connection conn = JDBCConnector.getConnection();
				if (conn != null) {
					PreparedStatement  ps = null;
					ResultSet rs = null;
					try {
						String query = " SELECT * FROM NUTCH_USER where username=? and password=?";
						ps = conn.prepareStatement(query);
						ps.setString(1, userName);
						ps.setString(2, PasswordUtil.encrypt(password, "Nutch"));
						rs = ps.executeQuery();
						if (rs != null && rs.next()) {
							logger.info("User has logged in successfully: "+userName);
							request.getSession(true);
							request.getSession().setAttribute("username", rs.getString("USERNAME"));
							roleId = rs.getInt("ROLE_ID");
							//Get RoleType from ROLE_MASTER table by passing ROLEID
							query = " SELECT * FROM ROLE_MASTER where ROLE_ID=?";
							ps = conn.prepareStatement(query);
							ps.setInt(1, roleId);
							rs = ps.executeQuery();
							if(rs != null && rs.next()){
								roleType=rs.getString("ROLE_NAME");
								request.getSession().setAttribute("rolename", roleType);
							}
							response.sendRedirect("/hm/index");
						}
						else{
							logger.info("User not availbale:");
							request.setAttribute("errorMessage", bundle.getString("credentialsInvalid"));
							request.getRequestDispatcher("/homepage.jsp").forward(request, response);
						}
					} catch (Exception e) {
						logger.error("Error while fetching user in NUTCH_USER" + e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
						request.getRequestDispatcher("/homepage.jsp").forward(request, response);
					} finally {
						try {
							if (ps != null) {
								ps.close();
							}
							if (conn != null) {
								conn.close();
							}
						} catch (SQLException e) {
							logger.error("Error while closing connection in logIn method:" + e.getMessage());
						}
					}
			  } else{
				  logger.error("Unable to get connection from DB: Please try after some time....");
				  request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
				  request.getRequestDispatcher("/homepage.jsp").forward(request, response);
			  }
			}else{
				logger.error("Please provide user credentials");
				request.setAttribute("errorMessage", bundle.getString("credentialsNotProvided"));
				request.getRequestDispatcher("/homepage.jsp").forward(request, response);
			}
		}
		
		else if (requestType.equalsIgnoreCase("logOut")) {
			logger.info("User logged out successfully:");
			request.getSession().removeAttribute("username");
			request.getSession().removeAttribute("rolename");
			request.getSession().invalidate();
			response.sendRedirect("/hm");
		}
		
		// MyAccount block
		else if (requestType.equalsIgnoreCase("myAccount")) {
			logger.info("User Account Information:");
			String rolename = (String) request.getSession().getAttribute("rolename");
			List<UserVO> userList = new ArrayList<UserVO>();
			if(rolename.equalsIgnoreCase("admin")){
				Connection conn = JDBCConnector.getConnection();
				if (conn == null) {
					logger.error("Connection not found in myAccount block:");
					request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
				}
				else{
					PreparedStatement  ps = null;
					ResultSet rs = null;
					ResultSet rolesResult = null;
					try {
						String query = " select * from NUTCH_USER";
						ps = conn.prepareStatement(query);
						rs = ps.executeQuery();
						while (rs.next()) {
							UserVO user = new UserVO();
							user.setUserName(rs.getString("USERNAME"));
							int rId = rs.getInt("ROLE_ID");
							//Get RoleType from ROLE_MASTER table by passing ROLEID
							query = " SELECT * FROM ROLE_MASTER where ROLE_ID=?";
							ps = conn.prepareStatement(query);
							ps.setInt(1, rId);
							rolesResult = ps.executeQuery();
							if(rolesResult != null && rolesResult.next()){
								roleType=rolesResult.getString("ROLE_NAME");
								user.setRoleName(roleType);
							}
							userList.add(user);
						}
						request.getSession().setAttribute("userList", userList);
					} catch (Exception e) {
						logger.error("Error while getting users from NUTCH_USER in myAccount block:" + e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
						request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
					} finally {
							try {
								if (ps != null) {
									ps.close();
								}
								if (conn != null) {
									conn.close();
								}
							} catch (SQLException e) {
								logger.error("Error while closing connection in myAccount block:" + e.getMessage());
							}
						}
				}
			}
			request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
		}
		
		//ChangePassword block
		else if (requestType.equalsIgnoreCase("ChangePwd")) {
			String userName = request.getParameter("userName");
			String password = request.getParameter("password");
			if(userName!=null && !userName.isEmpty()){
				if(password!=null && !password.isEmpty()){
				Connection conn = JDBCConnector.getConnection();
				if (conn == null) {
					logger.error("Connection not found in ChangePassword block:");
					request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
					request.getRequestDispatcher("/jsp/changePassword.jsp").forward(request, response);
				}
				else{
					PreparedStatement  ps = null;
					ResultSet rs = null;
					try {
						String query = " UPDATE NUTCH_USER SET password=? WHERE username = ?";
						ps = conn.prepareStatement(query);
						ps.setString(2, userName);
						ps.setString(1, PasswordUtil.encrypt(password, "Nutch"));
						rs = ps.executeQuery();
						conn.commit();
						if (rs != null && rs.next()) {
							logger.info("User password has updated successfully: "+userName);
							request.setAttribute("successMessage", bundle.getString("passwordUpdated"));
							request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
						}
					} catch (Exception e) {
						logger.error("Error while updating user in NUTCH_USER @ChangePassword block:" + e.getMessage());
						request.setAttribute("errorMessage",  bundle.getString("userUpdateError"));
						request.getRequestDispatcher("/jsp/changePassword.jsp").forward(request, response);
					} finally {
							try {
								if (ps != null) {
									ps.close();
								}
								if(conn != null){
									conn.close();
								}	
							} catch (SQLException e) {
								logger.error("Error while closing connection in ChangePwd block:" + e.getMessage());
							}
						}
					}
				}else{
					logger.error("Please provide user credentials");
					request.setAttribute("errorMessage", bundle.getString("passwordNotProvided"));
					request.setAttribute("uname", userName);
					request.getRequestDispatcher("/jsp/changePassword.jsp").forward(request, response);
				}
			}else{
				logger.error("Please provide user credentials");
				request.setAttribute("errorMessage", bundle.getString("userNameNotProvided"));
				request.getRequestDispatcher("/jsp/changePassword.jsp").forward(request, response);
			}
		}
		
		//Admin user adding new user
		else if (requestType.equalsIgnoreCase("AddUser")) {
			String uName = request.getParameter("childUserName");
			String pwd = request.getParameter("childPassword");
			String rType=request.getParameter("roleType");
			if(uName!=null && pwd!=null && !uName.isEmpty() && !pwd.isEmpty() && rType!=null && !rType.isEmpty()){
				Connection conn = JDBCConnector.getConnection();
				if (conn == null) {
					logger.error("Connection not found in AddUser block:");
					request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
					request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
				}
				else{
					PreparedStatement  ps = null;
					ResultSet rs = null;
					try {
						String checkUserQuery = "select * from NUTCH_USER where username="+"'"+uName+"'";
						String insertQuery = " INSERT INTO NUTCH_USER (username,password,role_id) VALUES (?,?,?)";
						ps = conn.prepareStatement(checkUserQuery);
						rs = ps.executeQuery();
						//User is already exist in database so not allowed to insert
						if(rs.next()){
							logger.error("User has already existed, Please try with other name:");
							request.setAttribute("errorMessage", bundle.getString("userExists"));
							request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
						}
						//if user not exists in Database then allowed to insert
						else{
							ps = conn.prepareStatement(insertQuery);
							ps.setString(1, uName);
							ps.setString(2, PasswordUtil.encrypt(pwd, "Nutch"));
							ps.setInt(3,Integer.parseInt(rType));
							rs = ps.executeQuery();
							conn.commit();
							if (rs != null && rs.next()) {
								logger.info("User has Added successfully: "+uName);
								request.setAttribute("successMessage", bundle.getString("userAdded"));
								request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
							}
						}
					} catch (Exception e) {
						logger.error("Error while adding user in NUTCH_USER in AddUser block:" + e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("addUserError"));
						request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
					} finally {
							try {
								if (ps != null) {
									ps.close();
								}
								if(conn != null){
									conn.close();
								}	
							} catch (SQLException e) {
								logger.error("Error while closing connection in AddUser block:" +e.getMessage());
							}
						}
				}
			}else{
				logger.error("Please provide user credentials");
				request.setAttribute("errorMessage", bundle.getString("credentialsNotProvided"));
				request.setAttribute("userRoles", CrawlUtil.userRoles());
				request.getRequestDispatcher("/jsp/addUser.jsp").forward(request, response);
			}
		}
		
		//Update User in NUTCH_USER table by passing username 
		else if (requestType.equalsIgnoreCase("updateUser")) {
			String userName = request.getParameter("userName");
			String newrole = request.getParameter("roleType");
			if(userName!=null  && !userName.isEmpty() && newrole!=null && !newrole.isEmpty()){
				Connection conn = JDBCConnector.getConnection();
				if (conn == null) {
					logger.error("Connection not found in updateUser block");
					request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
				}
				else{
					PreparedStatement  ps = null;
					ResultSet rs = null;
					try {
						String query = " UPDATE NUTCH_USER SET role_id=? WHERE username = ?";
						ps = conn.prepareStatement(query);
						ps.setInt(1, Integer.parseInt(newrole));
						ps.setString(2, userName);
						rs = ps.executeQuery();
						conn.commit();
						if (rs != null && rs.next()) {
							logger.info("User has updated successfully: "+userName);
							request.setAttribute("successMessage", bundle.getString("userUpdated"));
							request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
						}
					} catch (Exception e) {
						logger.error("Error while updating user in NUTCH_USER" + e.getMessage());
						request.setAttribute("errorMessage",  bundle.getString("userUpdateError"));
						request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
					} finally {
							try {
								if (ps != null) {
									ps.close();
								}
								if(conn != null){
									conn.close();
								}	
							} catch (SQLException e) {
								logger.error("Error while closing connection updateUser block:" + e.getMessage());
							}
					}
				}
			}else{
				logger.error("Please UserName not null");
				request.setAttribute("errorMessage", bundle.getString("userNameNotProvided"));
				request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
			}
		}
		
		//if user clicks on cancel operation
		else if (requestType.equalsIgnoreCase("Cancel")) {
			request.getSession().removeAttribute("errorMessage");
			request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
		}
		
	  } else{
		  logger.info("RequestType null:");
		  response.sendRedirect("/hm");
	  }
}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
}	

