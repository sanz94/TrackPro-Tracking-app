/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tracking;

import com.commondb.Common_DB;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author Admin
 */
@WebService(serviceName = "Tracking")
public class Tracking {

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "signin")
    public String signin(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
         try {
           Common_DB cd=new Common_DB();
            ResultSet rs=Common_DB.LoginCheck("psjav09", "tracking","username","password", username, password);
            if(rs.next()) {
            return "success";
            }
            else {
                return "username or password is invalid";
            }
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
            return "server temporarily not available";
        }       
    }    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "signup")
    public String signup(@WebParam(name = "username") String username, @WebParam(name = "password") String password, @WebParam(name = "email") String email) {
       try {
           Common_DB cd=new Common_DB();
            int rs=Common_DB.InsertTable("psjav09", "INSERT INTO tracking(username,password,email) VALUES('"+username+"','"+password+"','"+email+"')");
            if(rs>0) {
            return "success";
            }
            else {
                return "username is already exists";
            }
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
            return "server temporarily not available";
        }
        
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "updateStatus")
    public String updateStatus(@WebParam(name = "latitude") String latitude, @WebParam(name = "longitude") String longitude, @WebParam(name = "username") String username) {
        StringBuilder builder1 = new StringBuilder();
        try {
            int i = Common_DB.UpdateTable("psjav09","UPDATE tracking SET latitude='"+latitude+"',longitude='"+longitude+"',active='active' WHERE username='"+username+"'");
            if(i>0) {
                ResultSet rs = Common_DB.ViewParticularData("psjav09", "tracking", "username", username);
                if(rs.next()) {
                    String sndmsg = rs.getString("sendmessage");
                    if(sndmsg.length()>0){
                    String[] phonewithdistance = sndmsg.split("@");	
			StringBuilder builder = new StringBuilder();
                        if(phonewithdistance.length>0){
                            for(String extract : phonewithdistance) {
                                    String phonenumber = extract.substring(0,10);
                                    String distance = extract.substring(10,extract.length());
                                    double currentprediction = Double.parseDouble(distance)/5;
                                    double nextprediction = Double.parseDouble(distance)-currentprediction;
                                    builder.append(phonenumber+String.valueOf(nextprediction)+"@");
                                    //String latlon = CallService.trackerDistanceCalculator(phonenumber, username, "trackerDistanceCalculator");
                                    String latlon = trackerDistanceCalculator(phonenumber, username);
                                    builder1.append(phonenumber+"%"+distance+"#"+latlon+"&");
                            }
                        return builder1.toString();
                        }
                        else {
                            return "no tracker found";
                        }
                    }
                    else {
                         return "no tracker found";
                    }
                }
                
            }
            return "server down";
        } catch (Exception ex) {            
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "server down";
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getDeviceStatus")
    public String getDeviceStatus(@WebParam(name = "username") String username,@WebParam(name = "devicename") String devicename) {
        StringBuilder sb = new StringBuilder();
        String sendmsg = "";
        try {
            ResultSet rs = Common_DB.ViewParticularData("psjav09", "tracking", "username", devicename);
            if(rs.next()) {
              sb.append(rs.getString("latitude"));
              sb.append("@");
              sb.append(rs.getString("longitude")+"@");
              sendmsg = rs.getString("sendmessage");
            }
            ResultSet rs1 = Common_DB.ViewParticularData("psjav09", "tracking", "username", username);
             String send = "";
            if(rs1.next()){
             sb.append(rs1.getString("latitude"));
              sb.append("@");
              sb.append(rs1.getString("longitude"));  
              send = rs1.getString("phone")+"@"+sendmsg;
            }
            int insert = Common_DB.UpdateTable("psjav09", "UPDATE tracking SET sendmessage='"+send+"' WHERE username='"+devicename+"'");
            if(insert>0) {
                System.out.println("?????????? "+sb.toString());
                return sb.toString();
            }
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
            return sb.toString();
        }
        return sb.toString();
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getDeviceList")
    public String getDeviceList(@WebParam(name = "tablename") String tablename) {
       StringBuilder sb = new StringBuilder();
        try {
            ResultSet rs = Common_DB.ViewTable("psjav09", tablename);
            while(rs.next()) {
              sb.append(rs.getString("username"));
              sb.append("@");             
            }
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "updateMessage")
    public String updateMessage(@WebParam(name = "distance") String distance, @WebParam(name = "username") String username, @WebParam(name = "devicename") String devicename) {
        try {
            distance = distance.substring(0,distance.indexOf(" "));
             double dis=1;
            if(distance.length()>0){
               double currentprediction = Double.parseDouble(distance)/5;
               dis = Double.parseDouble(distance)-currentprediction;
            }
            String message = "";
            StringBuilder sb = new StringBuilder();
            ResultSet rs1 = Common_DB.ViewParticularData("psjav09", "tracking", "username", devicename);
                 String send = "";
                 if(rs1.next()){
                    String  message1 = rs1.getString("sendmessage");   
                    String[] extract = null;
                     if(message1.length()>5){   
                         message = message1;
                         extract = message.split("@");      
                     }
                 ResultSet rs2 = Common_DB.ViewParticularData("psjav09", "tracking", "username", username); 
                 if(rs2.next()) {
                    String phno = rs2.getString("phone");
                    if(extract!=null && extract.length>1) {
                        int ij = 0;
                        for(String number : extract){
                            if(number.length()>1) {
                            String currentnum = number.substring(0,10);
                            if((phno.equalsIgnoreCase(currentnum)) ) {  
                                if(ij==0) {
                                sb.append(currentnum+""+dis+"@");
                                ij = ij+1;
                                }
                                else {
                                    number = ""; 
                                }
                            }
                            else {
                                sb.append(number+"@");
                            }
                            }
                        }
                    }
                    else {
                        sb.append(phno+""+dis+"@"); 
                        System.out.println("********"+dis);
                    }
                 }
                 message = sb.toString();
                 }                 
                 int update = Common_DB.UpdateTable("psjav09", "UPDATE tracking SET sendmessage='"+message+"' WHERE username='"+devicename+"'");
            return ""+update;
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "predictedMessage")
    public String predictedMessage(@WebParam(name = "message") String message, @WebParam(name = "username") String username) {
        try {
            String[] phonewithdistance = message.split("@");	
	    StringBuilder builder = new StringBuilder();
            if(phonewithdistance.length>0){
               for(String extract : phonewithdistance) {
                   String phonenumber = extract.substring(0,10);
                   String distance = extract.substring(10,extract.length());
                   double currentprediction = Double.parseDouble(distance)/5;
                   double nextprediction = Double.parseDouble(distance)-currentprediction;
                   builder.append(phonenumber+String.valueOf(nextprediction)+"@");
                   String latlon = trackerDistanceCalculator(phonenumber, username);
                   //builder.append(phonenumber+distance+"@");
               }
            }          
            System.out.println("next threshold is "+message);
            int update = Common_DB.UpdateTable("psjav09", "UPDATE tracking SET sendmessage='"+builder.toString()+"' WHERE username='"+username+"'");
            return ""+update;
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "trackerDistanceCalculator")
    public String trackerDistanceCalculator(@WebParam(name = "trackerphonenumber") String trackerphonenumber, @WebParam(name = "username") String username) {
       StringBuilder sb = new StringBuilder();
        try {
            ResultSet rs = Common_DB.ViewParticularData("psjav09", "tracking", "username", username);
            if(rs.next()) {
              sb.append(rs.getString("latitude"));
              sb.append("@");
              sb.append(rs.getString("longitude")+"@");              
            }
            ResultSet rs1 = Common_DB.ViewParticularData("psjav09", "tracking", "phone", trackerphonenumber);
            if(rs1.next()){
              sb.append(rs1.getString("latitude"));
              sb.append("@");
              sb.append(rs1.getString("longitude"));                
            }
            return sb.toString();            
        } catch (Exception ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
            return sb.toString();
        }
    }
   
}
