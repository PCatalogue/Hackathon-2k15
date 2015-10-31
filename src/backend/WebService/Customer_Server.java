package Web_Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.security.auth.callback.Callback;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.Statement;

/**
 * Servlet implementation class Customer_Server
 */
@WebServlet("/Customer_Server")
public class Customer_Server extends HttpServlet {
	private static final long serialVersionUID = 1L;
    Connection con=null;
    java.sql.Statement st;
    String query;
    ResultSet rs;
    double customerLat;
    double customerLng;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Customer_Server() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void dbConnect() {
    	
    	try {
    		Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://113.128.164.167:3306/jit_dispatcher","root","root");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
    	double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (earthRadius * c);

        return dist/1000;
        }
    
    public JSONArray filterTechnicians(int cust_id, int product_id) throws JSONException {
		
    	JSONObject customer = new JSONObject();
    	JSONArray arr = new JSONArray();
    	JSONObject tech;
    	JSONObject res = new JSONObject();
    	try {
			st = con.createStatement();
			query ="select lat,lng from customer where cust_id ="+cust_id;
			rs = st.executeQuery(query);
			
			while(rs.next()){
				customerLat=rs.getDouble(1);
				customerLng=rs.getDouble(2);	
			}
			customer.put("CustomerLat", customerLat);
			customer.put("CustomerLng", customerLng);
			arr.put(customer);
			
			query = "select * from tech where status='available' and tech_id in(select tech_id from tech_prod where prod_id = "+product_id+")";
			rs = st.executeQuery(query);
			
			while(rs.next()){
				tech = new JSONObject();
				double techLat=rs.getDouble(4);
				double techlng=rs.getDouble(5);
				if(distFrom(customerLat, customerLng, techLat, techlng)<=5){
					tech.put("tech_id", rs.getInt(1));
					tech.put("techLat", rs.getDouble(4));
					tech.put("techLng", rs.getDouble(5));
					arr.put(tech);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//res.put("response", arr);
		return arr;
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		JSONArray jres = new JSONArray();
		
		System.out.println("Cust ID: "+request.getParameter("custID"));
		System.out.println("Type: "+request.getParameter("type"));
		System.out.println("Callback: "+request.getParameter("callback"));
		
		int cust_id = Integer.parseInt(request.getParameter("custID")); 
		int product_id= Integer.parseInt(request.getParameter("type"));
		String callback = "callbackForTechSupport";
		dbConnect();
		
		//JSONObject jsonObj = new JSONObject();
		
		try {
			jres = filterTechnicians(cust_id, product_id);
			//jsonObj = new JSONObject("{phonetype:N95, cat:WP}");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String res = callback+"("+jres+")";
		System.out.println(jres);
		
		//response.setContentType("application/json");
		response.getWriter().println(res);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
