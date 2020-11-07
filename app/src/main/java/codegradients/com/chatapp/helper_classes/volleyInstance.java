package codegradients.com.chatapp.helper_classes;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class volleyInstance {
    private static volleyInstance volleyInstance;
    private static Context context;
    private RequestQueue requestQueue;

    private volleyInstance(Context ctx){
        context  = ctx;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue(){
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized volleyInstance getmInstance(Context cont){
        if(volleyInstance == null){
            volleyInstance = new volleyInstance(cont);
        }
        return volleyInstance;
    }

    public<T> void addToRequestQueue(Request<T> request){
        request.setShouldCache(false);

        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getRequestQueue().add(request);
    }
}
