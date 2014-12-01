package com.hm.purge;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class PurgeDetailsServlet
 */
public class PurgeDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PurgeDetailsServlet() {
        super();
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String purgeId = (String)request.getParameter("PurgeID");
		PurgeEdgeCastData pData = new PurgeEdgeCastData();
        Map<String, String> purgeDetailsMap = pData.getPurgeIdDetails(purgeId);
		request.setAttribute("InDate", purgeDetailsMap.get("InDate"));
		request.setAttribute("CompleteDate", purgeDetailsMap.get("CompleteDate"));
        request.getRequestDispatcher("/purge.jsp").forward(request, response);
	}

}
