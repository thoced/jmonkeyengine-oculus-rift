package com.jme3.test.oculusvr;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.File;
import com.jme3.app.state.StereoCamAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeSystem;
import com.jme3.system.Natives;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oculusvr.input.OculusRiftReader;

public class TestStereoCams extends SimpleApplication {

    // set default for applets
    private static boolean useHttp = true;
    private static StereoCamAppState stereoCamAppState;
    private static OculusRiftReader orr;
    Spatial observer = new Node("");
    Node boxes = new Node("");
    
    boolean moveForward, moveBackwards, rotateLeft, rotateRight;
    Node scene;
    public static void main(String[] args) {
        String platform = JmeSystem.getPlatform().name();
        
        if(platform.startsWith("Win")){
            try {
                if(platform.endsWith("64")){
                    Natives.extractNativeLib("windows","OculusLib64", false, false);
                } else {
                    Natives.extractNativeLib("windows","OculusLib", false, false);
                }
                
            } catch (IOException ex) {
                System.out.println("failed to extract " + ex);
                Logger.getLogger(StereoCamAppState.class.getName()).log(Level.SEVERE, null, "Could not extract Oculus Rift library" + ex);
            }
        } else {
            try {
                throw new Exception("Sorry, platform not supported yet!");
            } catch (Exception ex) {
                Logger.getLogger(StereoCamAppState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        File file = new File("wildhouse.zip");
        if (file.exists()) {
            useHttp = false;
        }
        
        TestStereoCams app = new TestStereoCams();
        app.start();
        try {
            orr = new OculusRiftReader();
            StereoCamAppState.setOculusRiftReader(orr);
        } catch (Exception ex) {
            Logger.getLogger(TestStereoCams.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(10);
        Node mainScene=new Node();

        stereoCamAppState = new StereoCamAppState();
        
        stateManager.attach(stereoCamAppState);
        
        scene = new Node();
        if (useHttp) {
            assetManager.registerLocator("http://jmonkeyengine.googlecode.com/files/wildhouse.zip", HttpZipLocator.class);
        } else {
            assetManager.registerLocator("wildhouse.zip", ZipLocator.class);
        }
        scene.attachChild(assetManager.loadModel("main.scene"));
        Geometry box = new Geometry("", new Box(5,5,5));
        Material m = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        
        box.setMaterial(m);
        
        Geometry box2 = box.clone();
        box2.move(15, 0, 0);
        box2.setMaterial(m);
        Geometry box3 = box.clone();
        box3.move(-15, 0, 0);
        box3.setMaterial(m);
        
        
        boxes.attachChild(box);
        boxes.attachChild(box2);
        boxes.attachChild(box3);
        scene.attachChild(boxes);
        
        observer.setLocalTranslation(new Vector3f(0.0f, 0.0f, -10.0f));//observer.setLocalTranslation(new Vector3f(0,0,5));
        
        //observer.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        observer.addControl(stereoCamAppState.getCameraControl());
        mainScene.attachChild(observer);
        
        DirectionalLight sun = new DirectionalLight();
        Vector3f lightDir=new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f);
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        scene.addLight(sun);

        mainScene.attachChild(scene);
        rootNode.attachChild(mainScene);

        initInputs();
    }

     private void initInputs() {
//        inputManager.addMapping("toggle", new KeyTrigger(KeyInput.KEY_SPACE));
//        inputManager.addMapping("incShift", new KeyTrigger(KeyInput.KEY_ADD));
//        inputManager.addMapping("decShift", new KeyTrigger(KeyInput.KEY_SUBTRACT));
         inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping("back", new KeyTrigger(KeyInput.KEY_S));
         inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        ActionListener acl = new ActionListener() {

            public void onAction(String name, boolean keyPressed, float tpf) {
//                if (name.equals("toggle") && keyPressed) {
//                    if(enabled){
//                        oas.doubleSize();
//                    }
//                } 
//                else if(name.equals("incShift") && keyPressed){
//                    oas.changeProjectionShift(0.005f);
//                }else if(name.equals("incShift") && keyPressed){
//                    oas.changeProjectionShift(-0.005f);
//                }
                if(name.equals("forward")){
                    if(keyPressed){
                        moveForward = true;
                    } else {
                        moveForward = false;
                    }
                } else if(name.equals("back")){
                    if(keyPressed){
                        moveBackwards = true;
                    } else {
                        moveBackwards = false;
                    }
                }else if(name.equals("left")){
                    if(keyPressed){
                        rotateLeft = true;
                    } else {
                        rotateLeft = false;
                    }
                } else if(name.equals("right")){
                    if(keyPressed){
                        rotateRight = true;
                    } else {
                        rotateRight = false;
                    }
                }
                
                
            }
        };
        inputManager.addListener(acl, "forward");
        inputManager.addListener(acl, "back");
        inputManager.addListener(acl, "left");
        inputManager.addListener(acl, "right");
//        inputManager.addListener(acl, "toggle");
//        inputManager.addListener(acl, "incShift");
//        inputManager.addListener(acl, "decShift");
    }
     
     private float distance = 100f;
     private float prod = 0f;
    @Override
     public void update(){
         super.update();
         prod+=0.02f;
         distance = 100f * FastMath.sin(prod);
         boxes.setLocalTranslation(0, 0, 200f+ distance);
         
         if(moveForward){
             observer.move(stereoCamAppState.getCameraControl().getLookDirection().getRotationColumn(2).mult(1f));
         }
         if(moveBackwards){
             observer.move(stereoCamAppState.getCameraControl().getLookDirection().getRotationColumn(2).mult(-1f));
         }
         if(rotateLeft){
             observer.rotate(0, 0.05f, 0);
         }
         if(rotateRight){
             observer.rotate(0, -0.05f, 0);
         }
     }

    public static OculusRiftReader getOculusRiftReader(){
        return orr;
    }

    @Override
    public void destroy() {
        
        super.destroy();
        if(orr != null){
            orr.destroy();
        }
        
    }
    
    
}
