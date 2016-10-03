# TwitterSearchShubham


Twitter Search based API to search tweets and count the words ....does the  calculation of words in tweets


Issue:

1.The volley library hasn't been uploaded on Github

Requirements:

1.Volley Library has to be included in the project by using 
git clone https://android.googlesource.com/platform/frameworks/volley
rename the repository to volley-lib

2.The change in library has to be made:
public JsonObjectRequest(int method, String url, String requestBody, Listener<JSONObject> listener, ErrorListener errorListener) { super(method, url, requestBody, listener, errorListener); 
}
into JsonObjectRequest.java file



Shubham






