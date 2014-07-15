/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oculusvr.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import oculusvr.control.StereoCameraControl;
import com.jme3.post.Filter;
import com.jme3.post.SceneProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.TranslucentBucketFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.control.CameraControl;
import oculusvr.util.FilterUtil;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import oculusvr.shadow.OculusDirectionalLightShadowRenderer;
import java.util.List;
import oculusvr.input.HMDInfo;
import oculusvr.input.OculusRift;
import oculusvr.post.FastSSAO;
import oculusvr.post.OculusFilter;
import oculusvr.util.OculusGuiNode;

/**
 *
 * @author reden
 */
public class OVRAppState extends AbstractAppState {
    
    private SimpleApplication app;
    private FilterPostProcessor ppLeft, ppRight;
    private OculusFilter filterLeft, filterRight;
    Camera camLeft,camRight;
    ViewPort viewPortLeft, viewPortRight;
    private StereoCameraControl camControl = new StereoCameraControl();
    private HMDInfo info;
    private boolean flipEyes;
    private OculusGuiNode guiNode;
        
    public OVRAppState(boolean flipEyes) {
        OculusRift.setAppState(this);
        this.flipEyes = flipEyes;
        this.guiNode = null;
    }
    
    public OVRAppState(OculusGuiNode guiNode, boolean flipEyes) {
        OculusRift.setAppState(this);
        this.flipEyes = flipEyes;
        this.guiNode = guiNode;
    }
    
    public OVRAppState(OculusGuiNode guiNode) {
        OculusRift.setAppState(this);
        flipEyes = false;
        this.guiNode = guiNode;
    }   
    
    public OVRAppState() {
        OculusRift.setAppState(this);
        flipEyes = false;
        this.guiNode = null;
    }
    
    public OculusGuiNode getGuiNode() {
        return guiNode;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;

        viewPortLeft = app.getViewPort();
        camLeft = app.getCamera();
        
        camLeft.setFrustumPerspective(OculusRift.getFOV(), 
                                      (float) camLeft.getWidth() / camLeft.getHeight(),
                                      camLeft.getFrustumNear(), camLeft.getFrustumFar());                       
        
        if(camControl != null){
            camControl.setCamera(camLeft);
        }
        
        if(camRight == null){
            camRight = camLeft.clone();
            camControl.setCamera2(camRight);
            
        }
        camControl.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camLeft.setViewPort(0.0f, 0.5f, 0.0f, 1.0f);
        camRight.setViewPort(0.5f, 1f, 0.0f, 1f);
        viewPortRight = app.getRenderManager().createPostView("Right viewport", camRight);
        viewPortRight.setClearFlags(true, true, true);
        viewPortRight.attachScene(this.app.getRootNode());

        info = OculusRift.getHMDInfo();            
        
        AppSettings settings = this.app.getContext().getSettings();
        OculusRift.initRendering(settings.getWidth(), settings.getHeight(), settings.getSamples());
        
        filterLeft=new OculusFilter(OculusRift.loadedHmd, 0);
        filterLeft.setEyeRenderDesc(OculusRift.getEyeRenderDesc(0));
        filterRight =new OculusFilter(OculusRift.loadedHmd, 1);
        filterRight.setEyeRenderDesc(OculusRift.getEyeRenderDesc(1));
        
        ppRight =new FilterPostProcessor(app.getAssetManager());               
        ppLeft =new FilterPostProcessor(app.getAssetManager());
        
        ppLeft.addFilter(filterLeft);
        boolean hasTransFilter = false;
        
        for(SceneProcessor sceneProcessor : viewPortLeft.getProcessors()){
            if(sceneProcessor instanceof FilterPostProcessor){
                for(Filter f : ((FilterPostProcessor)sceneProcessor).getFilterList() ) {
                    ppLeft.addFilter(f);
                    if( f instanceof TranslucentBucketFilter ) {
                        hasTransFilter = true;
                    }
                }
                viewPortLeft.removeProcessor(sceneProcessor);
                break;
            }
        }
        
        if( hasTransFilter == false ) {
            ppLeft.addFilter(new TranslucentBucketFilter());
        }
        
        viewPortLeft.addProcessor(ppLeft);
        viewPortRight.addProcessor(ppRight);
        
        if( guiNode != null ) {
            guiNode.setupGui(viewPortLeft, viewPortRight);
        }
        
        ppRight.addFilter(filterRight);             
        
        float offset = info.getInterpupillaryDistance() * 0.5f;
        camControl.setCamHalfDistance(offset);
        
        cloneProcessors();        
        if( flipEyes ) {
            camControl.swapCameras();
        }
    }  
    
    @Override
    public void update(float tpf) {
        super.update(tpf);        
        
    }
    
    @Override
    public void postRender() {
        super.postRender();
    }   
        
    public void setCameraControl(StereoCameraControl control){
        this.camControl = control;
        camRight = control.getCamera2();
    }
    
    public StereoCameraControl getCameraControl(){
        return camControl;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        OculusRift.destroy();
    }
    
    public void cloneProcessors(){
        List<SceneProcessor> processors = viewPortLeft.getProcessors();
        for(SceneProcessor sp: processors){
            if(sp instanceof FilterPostProcessor){
                FilterPostProcessor fpp1 = (FilterPostProcessor) sp;
                OculusFilter bdf = ppRight.getFilter(OculusFilter.class);
                ppRight.removeFilter(bdf);
                for(Filter filter: fpp1.getFilterList()){
                                        
                    Filter f2 = null;
                    if(filter instanceof FogFilter){
                        f2 = FilterUtil.cloneFogFilter((FogFilter)filter);
                        
                    } 
                    else if (filter instanceof FastSSAO) {
                        f2 = new FastSSAO((FastSSAO)filter);
                    }
                    //else if (filter instanceof WaterFilter){
                    //    f2 = ((WaterFilter)filter) //doesn't seem to be a clone function ready to go?
                    //} 
                    else if (filter instanceof SSAOFilter){
                        f2 = FilterUtil.cloneSSAOFilter((SSAOFilter)filter);
                    } else if (filter instanceof DirectionalLightShadowFilter){
                        f2 = FilterUtil.cloneDirectionalLightShadowFilter(app.getAssetManager(), (DirectionalLightShadowFilter)filter);
                    } 
                    else if (!(filter instanceof OculusFilter)){
                        f2 = filter; // dof, bloom, lightscattering
                    }
                    
                    if(f2 != null) ppRight.addFilter(f2);                    
                }
                ppRight.addFilter(bdf);
            } else if (sp instanceof OculusDirectionalLightShadowRenderer){
                OculusDirectionalLightShadowRenderer dlsr = (OculusDirectionalLightShadowRenderer) sp;
                
                OculusDirectionalLightShadowRenderer dlsrRight = dlsr.clone();
                dlsrRight.setLight(dlsr.getLight());
                
                viewPortRight.getProcessors().add(0, dlsrRight);
            }
        }
    }
    
    public ViewPort getLeftViewPort(){
        return viewPortLeft;
    }
    
    public ViewPort getRightViewPort(){
        return viewPortRight;
    }
    
}