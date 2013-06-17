/**
 * Created by the U.S. Department of Labor
 * This source is released to the Public domain
 */
package govdata.api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;

import com.google.common.base.Strings;

public class GOVAPIUtils {
	
	public static String getRequestHeader(String apiURI, String apiHost, String apiKey, String sharedSecret) throws NoSuchAlgorithmException, InvalidKeyException
	{
		//Timestamp
		String timestamp = getAPITimestamp();
		
		//Remove API_HOST from the apiURI
		//That part is not signed
		apiURI = apiURI.substring(apiHost.length());
		
		//Signature
		MessageFormat formatter = new MessageFormat("{0}&Timestamp={1}&ApiKey={2}");
		String dataToSign = formatter.format(new String[] {apiURI, timestamp, apiKey});
		String signature = getAPISignature(dataToSign, sharedSecret);
		
		//Final header
		formatter = new MessageFormat("Timestamp={0}&ApiKey={1}&Signature={2}");
		String result = formatter.format(new String[] {timestamp, apiKey, signature});
		
		return result;
	}
	
	@SuppressLint("SimpleDateFormat")
	private static String getAPITimestamp()
	{
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		//Time in GMT
		return dateFormatGmt.format(new Date());
	}
	
	private static String getAPISignature(String data, String sharedSecret) throws NoSuchAlgorithmException, InvalidKeyException
	{
	    byte[] keyData = sharedSecret.getBytes();
	    
	    // Create the key
	    SecretKeySpec key = new SecretKeySpec(keyData, "HmacSHA1");

	    // Now an HMAC can be created, passing in the key and the
	    // SHA digest.
	    Mac mac = Mac.getInstance("HmacSHA1");
	    mac.init(key);

	    // The HMAC can be updated much like a digest
	    byte[] sign = mac.doFinal(data.getBytes());
	 
	    // Create Hex String from Byte array
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i< sign.length; i++)
        	hexString.append(String.format("%02x", sign[i]));
        return hexString.toString();
	}
	
	public static List<Map<String, String>> parseJSON(String jsonString) {
		try {
			//all objects are prefixed with o for oData.
			
			//We'll store the list of Maps here
			List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
			
			//Get JSON Object, original parse of root object
			JSONObject object = new JSONObject(jsonString);
			
			//A list of paths that will need to be flattened.
			List<String> oPaths =new ArrayList<String>();
			
			//Current list of keys for the root object.  oMap will be used when a node has been resolved
			JSONArray oMap = object.names();
			JSONArray oArr; //temporary object to handle JSON array data.	
			
			Map<String,String> map;//key, value pair
			
			//encode root paths
			map = new HashMap<String,String>();
			for(int i=0; i<oMap.length();i++)
			{
				//all keys are strings, lets get the name of the key first.
				String oKey = oMap.optString(i);
			
				//lets test if the object that was returned is an array.
				oArr = object.optJSONArray(oKey);
				if(null !=oArr)
				{
					//Lets assume that all items in an array are the same type, then test if this an object array.
					if(null != oArr.optJSONObject(0))
					{
						//if so add each object in the array to the path list.
						for(int j=0;j<oArr.length(); j++)
						{
							//We add the pipe to separate the index from the key name.  This will later be used to construct a path from the root object.
							oPaths.add(oKey + "|" + Integer.toString(j));
						}
					} else {
						//could be an array of primitives
						for(int j=0;j<oArr.length(); j++)
						{
							//lets support only primitive arrays for now.
							 //need to generate some kind of naming convention for arrays, otherwise the Map object will overwrite entries.
							 map.put(oKey + "|" + Integer.toString(j), oArr.optString(j));
						}
					}
					
					continue;
				}
				
				//if this node is an object add it to the path list.
				if(null != object.optJSONObject(oKey))
				{
					oPaths.add(oKey);
					continue;
				}

				//if this node is a primitive, add it to the result list 
				String oVal = object.optString(oKey);
				if(null != oVal)
				{
					 map.put(oKey, oVal);
					continue;
			    }
			}//end root path encoding
			
			//add all properties for this object to results list.
			    resultList.add(map);
						
			//now that the root list has been added its time to cycle through this list.
			for(int i=0; i<oPaths.size();i++)
			{
				JSONObject oObj;
				oObj = navigateJson(oPaths.get(i), object);

				
				if(null != oObj)
				{
					map = new HashMap<String,String>();
					//get the list of key for this object.
					oMap = oObj.names();
					for(int j=0; j<oMap.length();j++)
					{
						//get key name for later.
						String oKey =  oMap.optString(j);
						
						//test to see if this key is an array
						oArr = oObj.optJSONArray(oKey);
						if(null !=oArr)
						{
							//test if this an object array.
							if(null != oArr.optJSONObject(0))
							{
								//if so add each object path to the collection
								for(int k=0;k<oArr.length(); k++)
								{
									oPaths.add(oPaths.get(i) + "/" + oKey + "|" + Integer.toString(k));
								}
							} else {
								//could be an array of primitives
								for(int k=0;j<oArr.length(); k++)
								{
									//lets support only primitive arrays for now.
									//need to generate some kind of naming convention for arrays, otherwise the Map object will overwrite entries.
									 map.put(oKey + "|" + Integer.toString(k), oArr.optString(j));
								}
							}		
							
							continue;
						}
						
						//test if the item is a nested object, if so add it to the path list with the current path.
						if(null != oObj.optJSONObject(oKey))
						{
							oPaths.add(oPaths.get(i) + "/" + oKey);
							continue;
			}
			
						//test if item is a primitive value if so add it to the map.
						String oVal = oObj.optString(oKey);
						if(null != oVal)
						{
							 map.put(oKey, oVal);
							continue;
						}
					}//end name loop
					
					//add object map to list of results.
					resultList.add(map);
				} //end of object
			}// end of path loop
			
			return resultList;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//navigates the JSON DOM starting from the root object given a path to the object node.
	private static JSONObject navigateJson(String path, JSONObject rootObj)
	{
		//need to reconstruct the path since there is no traversing mechanism for the android JSON object tokenizer. 
		StringTokenizer token = new StringTokenizer(path, "/");
		JSONObject oObj;
		
		//while loop is used to traverse the path to find the object or an array. Array have a pipe to separate the index and the key name.
		//start at the root object.
		oObj = rootObj;
		while(token.hasMoreTokens())
		{
			String t = token.nextToken();
			int pipe = t.indexOf("|");  //needed a way to select an index on an array to go deeper.
			if(0 < pipe) //only arrays will have Pipe
			{
				String  KeyName = t.substring(0, pipe);
				int _i=Integer.parseInt(t.substring( pipe +1));
				oObj = oObj.optJSONArray(KeyName).optJSONObject(_i);
			} else {
				oObj = oObj.optJSONObject(t);
			}
		}
		
		return oObj;
	}
	
	@SuppressWarnings("null")
	public static Map<String, String> parseXML(String xmlString) {
		Map<String, String> resultList = null;
		try{
		     DocumentBuilderFactory f =
		     DocumentBuilderFactory.newInstance();
		     DocumentBuilder b = f.newDocumentBuilder();
		     Document doc = b.parse(xmlString);
		     NodeList nodeList = doc.getChildNodes();
		     
		     for (int i = 0; i < nodeList.getLength(); i++) {
		          Node textChild = nodeList.item(i);
		          NodeList childNodes = textChild.getChildNodes();
		          
		     for (int j = 0; j < childNodes.getLength(); j++) {
		          
		    	      Node grantChild = childNodes.item(j);
		              NodeList grantChildNodes = grantChild.getChildNodes();
		              for (int k = 0; k < grantChildNodes.getLength(); k++) {
		                  if(Strings.isNullOrEmpty(grantChildNodes.item(k).getTextContent() ) ) {
		                      Map<String, String> map = new HashMap<String, String>();
		                              map.put(grantChildNodes.item(k).getNodeName() , grantChildNodes.item(k).getTextContent());
		                              ((ArrayList<Map<String,String>>) resultList).add(map);
		                  }
		              }
		          }
		      }
		      
		      
	}
	  catch (Exception e)
		    {
		      e.printStackTrace();
		    }
	
		  
		return resultList;
	
			
		}
		

	
	/** 
		 * @deprecated Do not use this method! 
	 */  
	public static List<Map<String, String>> parseJSON_v1(String jsonString) {
	try {
		//Get JSON Object
		JSONObject object = new JSONObject(jsonString);
		
		//pass the "d" security wrapper
		JSONArray wrapper = object.optJSONArray("d");
		
		//Special case when .Net adds an extra "results" wrapper
		if (wrapper == null) {
			object = object.getJSONObject("d");
			wrapper = object.optJSONArray("results");
		}
		
		//We'll store the list of Maps here
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		
		//Loop through all the results and store them in the map
		for (int i = 0; i < wrapper.length(); i++) {
			JSONObject obj = wrapper.getJSONObject(i);

			Map<String,String> map = new HashMap<String,String>();
			
			//Iterate through all the columns. Convert JSONObject to Map
			Iterator<?> iter = obj.keys();
			while(iter.hasNext()){
				String key = (String)iter.next();
				String value = obj.getString(key);
				map.put(key,value);
			}
			resultList.add(map);
		}
		
		return resultList;
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
}
	
}
