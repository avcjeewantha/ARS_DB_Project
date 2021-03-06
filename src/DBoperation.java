import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class DBoperation {
	String url = "jdbc:mysql://localhost:3307/airlinereservationsys";
    String password="";
    String username = "root";
    Connection con =null;
    PreparedStatement pst=null;
    ResultSet rs;
    
    public boolean checkforadminlogin(String uname,String pw) {
    	try {
    		//System.out.println("in db operation class");
			con = (Connection)DriverManager.getConnection(url, username, password);
			String query= "select * from user where username=? and password=? and user_category_id=?";
	        pst =(PreparedStatement)con.prepareStatement(query);
	        pst.setString(1, uname);
	        pst.setString(2, pw);
	        pst.setString(3, "0");
	        rs=pst.executeQuery();
	    
	        while(rs.next()) {
	        	return true;
	        }
	        
	        Login.error="Invalid username or password!";
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Login.error="Error occured!";
		}
        
    	return false;
    }
    
    
   public boolean checkforuserlogin(String uname,String pw) {
    	try {
    		//System.out.println("in db operation class");
			con = (Connection)DriverManager.getConnection(url, username, password);
			String query= "select * from user where username=? and password=? and (user_category_id=? or user_category_id=?)";
	        pst =(PreparedStatement)con.prepareStatement(query);
	        pst.setString(1, uname);
	        pst.setString(2, pw);
	        pst.setString(3, "1");
	        pst.setString(4, "2");
	        rs=pst.executeQuery();
	    
	        while(rs.next()) {
	        	
	        	return true;
	        }
	        
	        Login.error="Invalid username or password!";
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			Login.error="Error occured!";
			e.printStackTrace();
		}
        
    	return false;
    }
    
    public ArrayList<Flight> getScheduleHistory(String from_port,String to_port) {
    	ArrayList<Flight> past_flights=new ArrayList<>();
    	
    	try {
    		   
			Connection con = (Connection)DriverManager.getConnection(url, username, password);
			String query_from="select airport_code from airport where name=?";
			String query_to="select airport_code from airport where name=?";
			
			PreparedStatement pst_from =(PreparedStatement)con.prepareStatement(query_from);
			PreparedStatement pst_to =(PreparedStatement)con.prepareStatement(query_to);
			
			pst_from.setString(1, from_port);
	        pst_to.setString(1, to_port);
			
	        ResultSet rs_from=pst_from.executeQuery();
	        ResultSet rs_to=pst_to.executeQuery();
	        
	        String from_port_id=null;
	        String to_port_id=null;
	        while(rs_from.next()) { from_port_id=rs_from.getString(1);}
	        while(rs_to.next()) {to_port_id=rs_to.getString(1);}
	        
	    //    if(!(from_port_id.equals(null) || to_port_id.equals(null))) {
				String query= "Select schedule_id,date,flight_id,booked_seats_business+booked_seats_platinum+booked_seats_econ,airplane_id from schedule where date<=Date(Now()) and flight_id in (Select flight_id from flight inner join route using(route_id) where from_port_id=? and to_port_id=?)";
		        PreparedStatement pst =(PreparedStatement)con.prepareStatement(query);
		        pst.setString(1, from_port_id);
		        pst.setString(2, to_port_id);
		        ResultSet rs=pst.executeQuery();
		        
		     
		        while(rs.next()) {
		        	 Flight pf=new Flight();
		        	
		        	 pf.setDate(rs.getString(2));
	                 pf.setFlight_id(rs.getString(3));
	                 pf.setSchedule_id(rs.getString(1));
	                 pf.setPassenger_count(rs.getString(4));
	                 pf.setAirplane_id(rs.getString(5));
	                 past_flights.add(pf);
		        }
	      //  }else {
	        	
	      //  }    
	        con.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return past_flights;
        
    }
    
    public ArrayList<ArrayList<Passenger>> getNextFlightInfo(String flight_id) {
    	ArrayList<ArrayList<Passenger>> returnArray=new ArrayList<>();
    	ArrayList<Passenger> passenger_list_above18=new ArrayList<>();
    	ArrayList<Passenger> passenger_list_below18=new ArrayList<>();
    	try {
			Connection con = (Connection)DriverManager.getConnection(url, username, password);
			String query= "select schedule_id from schedule where flight_id =? and date>=Date(Now()) order by date asc limit 1";
	        PreparedStatement pst =(PreparedStatement)con.prepareStatement(query);
	        pst.setString(1, flight_id);
	       
	        ResultSet rs=pst.executeQuery();
	        
	        while(rs.next()) {
	        	String schedule_id=rs.getString(1);
	        	String query_2="select * from user where user_id in (select user_id from booking where schedule_id=?)";
	        	PreparedStatement pst_2 =(PreparedStatement)con.prepareStatement(query_2);
	        	pst_2.setString(1, schedule_id);
	        	
	        	ResultSet rs_2=pst_2.executeQuery();
	        	
	        	while(rs_2.next()) {
	        		Passenger passenger=new Passenger();
	        		passenger.setName(rs_2.getString(4)+" "+rs_2.getString(5));
	                passenger.setEmail(rs_2.getString(7));
	                passenger.setAddress(rs_2.getString(9));
	                passenger.setAge(rs_2.getString(10));
	                if(new Integer(rs_2.getString(10))>=18) {
	                	 passenger_list_above18.add(passenger);
	                }else {
	                	passenger_list_below18.add(passenger);	
	                }
	               
	        	}
	        }
	        con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	returnArray.add(passenger_list_above18);returnArray.add(passenger_list_below18);
    	return returnArray;
    }
    
    public String getDestinationPassengerCount(String destination,Date from_date,Date to_date) {
    	try {
			Connection con = (Connection)DriverManager.getConnection(url, username, password);
			
			String query_destination="select airport_code from airport where name=?";
			PreparedStatement pst_destination =(PreparedStatement)con.prepareStatement(query_destination);
			pst_destination.setString(1, destination);
			ResultSet rs_destination=pst_destination.executeQuery();
			
			String destination_id=null;
			while(rs_destination.next()) { destination_id=rs_destination.getString(1);}
			
			String query= "select sum(booked_seats_econ)+sum(booked_seats_business)+sum(booked_seats_platinum) from schedule where date>=? and date<=? and flight_id in (select flight_id from flight where route_id in (select route_id from route where to_port_id=?))";
	        PreparedStatement pst =(PreparedStatement)con.prepareStatement(query);
	        
	        Timestamp date1=new Timestamp(from_date.getTime());
	        Timestamp date2=new Timestamp(to_date.getTime());
	        pst.setTimestamp(1, date1);
	        pst.setTimestamp(2, date2);
	        
	        pst.setString(3, destination_id);
	        
	        ResultSet rs=pst.executeQuery();
	        while(rs.next()) {
	        	
                return rs.getString(1);
	        }
	        con.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
<<<<<<< HEAD
    	
    	return null;
    }
    
    public HashMap<String,String> getPassengerTypeCount(Date from_date,Date to_date) {
    	HashMap<String,String> category_counts=new HashMap<>();
    	try {
			Connection con = (Connection)DriverManager.getConnection(url, username, password);
			String query= "select category_name,count(user_category_id) from booking inner join (select * from user inner join user_category on user.user_category_id=user_category.category_id) c on booking.user_id=c.user_id where schedule_id in (select schedule_id from schedule where date>=? and date<=?) group by user_category_id";
	        PreparedStatement pst =(PreparedStatement)con.prepareStatement(query);
	        
	        Timestamp date1=new Timestamp(from_date.getTime());
	        Timestamp date2=new Timestamp(to_date.getTime());
	        pst.setTimestamp(1, date1);
	        pst.setTimestamp(2, date2);
	        
	        ResultSet rs=pst.executeQuery();
	        while(rs.next()) {
	        	category_counts.put(rs.getString(1),rs.getString(2));
	        }
	        con.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return category_counts;
    }
    
    public String getTotalAircraftRevenue() {
    	
		try {
			Connection con = (Connection)DriverManager.getConnection(url, username, password);
			String query="select * from airplane group by (type_id)";
	        PreparedStatement pst =(PreparedStatement)con.prepareStatement(query);
			
			ResultSet rs=pst.executeQuery();
			while(rs.next()) {
				System.out.println(rs.getString(1));
				System.out.println(rs.getString(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    	return "0";
=======
    	System.out.println(past_flights);
    	return past_flights;
        
>>>>>>> 67e1b920000f044d2555bfb8e95bebce39465e2d
    }
    
    
    public boolean addUser(user em){
        System.out.println(em.getFirstname()+" "+em.getFirstname().length());
        try{
           
            con = (Connection)DriverManager.getConnection(url, username, password);
            
            String query= "INSERT INTO user values (?,?,?,?,?,?,?,?,?,?)";
            pst =(PreparedStatement)con.prepareStatement(query);
            
            pst.setInt(1,em.getUser_id());
            pst.setString(2,em.getUsername());
            pst.setString(3,em.getPassword());
            pst.setString(4,em.getFirstname());
            pst.setString(5,em.getLastname());
            pst.setString(6, em.getUser_category_id());
            pst.setString(7,em.getEmail());
            pst.setString(8,em.getPhonenumber());
            pst.setString(9,em.getAddress());
            pst.setInt(10,em.getAge());
            
            
            
            pst.executeUpdate();
            
            Register.error="";
            
            return true;
            
        }catch(Exception e){
            System.out.println(e);
            
            Register.error=e.getMessage().substring(33,e.getMessage().length()-16);
            System.out.println(Register.error);
            
            return false;
        }finally{
            try{
                if(pst != null){
                    pst.close();
                }
                if (con != null){
                    con.close();
                }
            }catch(Exception e){
            
            }
        }
    }
    
    
    int checkforuser(user em){
        try{
            con = (Connection)DriverManager.getConnection(url, username, password);
          
            String query= "select username from user";
            pst =(PreparedStatement)con.prepareStatement(query);
            
            rs = pst.executeQuery();
            
            while(rs.next()){
                if(em.getUsername().equals(rs.getString(1))){
                	 Register.error="username already exists!!!";
                     return 1;
                }
            }
            
            return 0;
            
        }catch(Exception e){
            System.out.println(e);
            
            return -1;
        }finally{
            try{
                if(pst != null){
                    pst.close();
                }
                if (con != null){
                    con.close();
                }
            }catch(Exception e){
            
            }
        }
    }
    
    
    
 ArrayList<Shedule> getShedule(){
        
        ArrayList<Shedule> list=new ArrayList<Shedule>();
        
        try{
           
            con = (Connection)DriverManager.getConnection(url, username, password);
           
            String query= "select  date,flight_id,airplane_id,b.name,c.name,departure_time,new_departure_time,schedule_id from schedule left join delay using (delay_id)  left join flight using(flight_id) left join route using (route_id),airport as b, airport as c where b.airport_code= from_port_id and c.airport_code = to_port_id";
            
            pst =(PreparedStatement)con.prepareStatement(query);
            
            
            rs = pst.executeQuery();
            
            while(rs.next()){
                Shedule em=new Shedule();
                
                em.setDate(rs.getString(1));
                em.setAirline(rs.getString(2));
                em.setFlight(rs.getString(3));
                em.setFrom(rs.getString(4));
                em.setTo(rs.getString(5));
                em.setSheduled_time(rs.getString(6));
                em.setDelay_time(rs.getString(7));
                em.setSheduleid(rs.getString(8));
                
                
                
                list.add(em);
                
            }
            
            return list;
            
        }catch(Exception e){
            System.out.println(e);
            
            return null;
        }finally{
            try{
                if(pst != null){
                    pst.close();
                }
                if (con != null){
                    con.close();
                }
            }catch(Exception e){
            
            }
        }
        
        
    
    }
 
 
 Price getPrices(String sheduleid){
    
     try{
         Price em=new Price();
         con = (Connection)DriverManager.getConnection(url, username, password);
        
         String query= "select econ_price,business_price,platinum_price from schedule left join price using(price_id) where schedule_id="+sheduleid;
         pst =(PreparedStatement)con.prepareStatement(query);
        // pst.setString(1,ss.getSheduleid());
         
         rs = pst.executeQuery();
         
         while(rs.next()){
             
             
             em.setEcon_price(rs.getFloat(1));
             em.setBusiness_price(rs.getFloat(2));
             em.setPlatinum_price(rs.getFloat(3));
             
             
         }
         
         return em;
         
     }catch(Exception e){
         System.out.println(e);
         return null;
        
     }finally{
         try{
             if(pst != null){
                 pst.close();
             }
             if (con != null){
                 con.close();
             }
         }catch(Exception e){
         
         }
     }
 }
 
 double getDiscount(String uname) {
	 try{
         
         con = (Connection)DriverManager.getConnection(url, username, password);
        
         String query= "select discount from user left join user_category on user.user_category_id=user_category.category_id where username="+uname;
         pst =(PreparedStatement)con.prepareStatement(query);
        // pst.setString(1,ss.getSheduleid());
         
         rs = pst.executeQuery();
         
         double discount=0.5;
         
         while(rs.next()){
             discount= Double.parseDouble(rs.getString(1));
         }
         
         return discount;
         
     }catch(Exception e){
         System.out.println(e);
         return 0.5;
        
     }finally{
         try{
             if(pst != null){
                 pst.close();
             }
             if (con != null){
                 con.close();
             }
         }catch(Exception e){
         
         }
     }
	 
 }
 
 
}