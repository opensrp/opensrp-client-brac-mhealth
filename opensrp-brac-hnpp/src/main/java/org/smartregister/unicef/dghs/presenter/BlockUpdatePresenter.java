package org.smartregister.unicef.dghs.presenter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.dghs.contract.BlockUpdateContract;
import org.smartregister.unicef.dghs.utils.Node;

import java.util.ArrayList;
import java.util.HashMap;


public class BlockUpdatePresenter implements BlockUpdateContract.Presenter {
    private static final String GET_BLOCK_URL = "/rest/event/user/ward/block?";
    private static final String UPDATE_BLOCK_URL = "/save/user/block?";
    AppExecutors appExecutors;
    BlockUpdateContract.View view;
    ArrayList<Node>nodeList = new ArrayList<>();
    HashMap<View, Node> nodeView = new HashMap<>();
    public BlockUpdatePresenter(BlockUpdateContract.View view){
        this.view = view;
        appExecutors = new AppExecutors();
        nodeList = new ArrayList<>();
    }

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    public HashMap<View, Node> getNodeView() {
        return nodeView;
    }

    @Override
    public void getBlock() {
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                JSONArray jsonArray = getBlockListFromServer();
                Log.v("JSON array: ",jsonArray+"");

                try{
                    nodeList = new ArrayList<>();
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject ward = jsonArray.getJSONObject(i);
                        Node node = new Node();
                        node.name = ward.getString("name");
                        JSONArray nodes = ward.getJSONArray("node");
                        for(int k=0;k<nodes.length();k++){
                            JSONObject block = nodes.getJSONObject(k);
                            Node childNode = new Node();
                            childNode.name = block.getString("name");
                            childNode.nodes = null;
                            childNode.parent = node;
                            childNode.vaild = block.getBoolean("assigned");
                            node.nodes.add(childNode);
                        }
                        nodeList.add(node);
                    }
                }catch(Exception e){
                }

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.updateAdapter();
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void updateBlock(String newBlocks) {

        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                JSONObject jsonObject = submitBlocks(newBlocks);
                Log.v("JSON array: ",jsonObject+"");

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.onBlockUpdated();
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public BlockUpdateContract.View getView() {
        return view;
    }

    private JSONObject submitBlocks(String blocks){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return null;
            }
            String password = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityId(userName);
            //testing
            String url = baseUrl + UPDATE_BLOCK_URL + "username=" + userName+"&password="+password+"&blocks="+blocks;

            Log.v("NOtification Fetch","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(UPDATE_BLOCK_URL + " not returned data");
            }

            return new JSONObject((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }

    private JSONArray getBlockListFromServer(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return null;
            }
            //testing
            String url = baseUrl + GET_BLOCK_URL + "username=" + userName;

            Log.v("NOtification Fetch","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(GET_BLOCK_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }

}
