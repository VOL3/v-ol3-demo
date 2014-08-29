package org.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import org.vaadin.addon.vol3.OLMap;
import org.vaadin.addon.vol3.OLView;
import org.vaadin.addon.vol3.OLViewOptions;
import org.vaadin.addon.vol3.client.OLCoordinate;
import org.vaadin.addon.vol3.client.Projections;
import org.vaadin.addon.vol3.client.control.OLMousePositionControl;
import org.vaadin.addon.vol3.client.control.OLScaleLineControl;
import org.vaadin.addon.vol3.client.source.OLMapQuestLayerName;
import org.vaadin.addon.vol3.feature.OLFeature;
import org.vaadin.addon.vol3.feature.OLGeometry;
import org.vaadin.addon.vol3.feature.OLLineString;
import org.vaadin.addon.vol3.feature.OLPoint;
import org.vaadin.addon.vol3.interaction.*;
import org.vaadin.addon.vol3.layer.OLTileLayer;
import org.vaadin.addon.vol3.layer.OLVectorLayer;
import org.vaadin.addon.vol3.source.OLMapQuestSource;
import org.vaadin.addon.vol3.source.OLSource;
import org.vaadin.addon.vol3.source.OLVectorSource;

import javax.servlet.annotation.WebServlet;
@Title("OpenLayers 3 demo")
@Theme("mytheme")
@SuppressWarnings("serial")
public class OL3Demo extends UI
{

    @WebServlet(value = "/*", asyncSupported = true, displayName = "Open layers 3 demo")
    @VaadinServletConfiguration(productionMode = false, ui = OL3Demo.class, widgetset = "org.vaadin.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    private OLMap map;
    private OLTileLayer baseLayer;
    private OLVectorLayer vectorLayer;

    @Override
    protected void init(VaadinRequest request) {
        // create root layout
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        setContent(layout);
        // create map instance
        map=new OLMap();
        map.setView(createView());
        map.setSizeFull();
        layout.addComponent(map);
        layout.setExpandRatio(map, 1.0f);
        // add layers to map
        baseLayer=new OLTileLayer(createTileSource());
        map.addLayer(baseLayer);
        vectorLayer=new OLVectorLayer(createVectorSource());
        vectorLayer.setLayerVisible(false);
        map.addLayer(vectorLayer);
        // create footer controls
        layout.addComponent(createFooterControls());
        // add some map controls
        createMapControls();
        // add initial interaction
        map.addInteraction(new OLSelectInteraction());
    }

    private OLView createView(){
        OLViewOptions options=new OLViewOptions();
        OLView view=new OLView(options);
        view.setZoom(1);
        view.setCenter(0,0);
        return view;
    }

    private OLSource createTileSource(){
        return new OLMapQuestSource(OLMapQuestLayerName.OSM);
    }

    private OLSource createVectorSource() {
        OLVectorSource source=new OLVectorSource();
        source.addFeature(createPointFeature(-50,0));
        source.addFeature(createPointFeature(50,0));
        source.addFeature(createPointFeature(-50,50));
        source.addFeature(createPointFeature(50,50));
        source.addFeature(createRectangleFeature("rect",-50,0,100,50));
        return source;
    }

    private void createMapControls(){
        map.setMousePositionControl(new OLMousePositionControl());
        map.getMousePositionControl().projection= Projections.EPSG4326;
        map.setScaleLineControl(new OLScaleLineControl());
    }

    private ComponentContainer createFooterControls(){
        HorizontalLayout controls=new HorizontalLayout();
        controls.setSpacing(true);
        controls.setMargin(true);
        // create button that toggles vector layer
        Button toggleVector=new Button("toggle vector layer");
        toggleVector.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                vectorLayer.setLayerVisible(!vectorLayer.isLayerVisible());
            }
        });
        controls.addComponent(toggleVector);
        // create button that resets view
        Button resetView=new Button("reset view");
        resetView.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                map.getView().setCenter(0,0);
                map.getView().setZoom(1);
            }
        });
        controls.addComponent(resetView);
        // create chooser for current interaction mode
        ComboBox interactionMode=new ComboBox();
        interactionMode.addItem("select mode");
        interactionMode.addItem("edit mode");
        interactionMode.addItem("draw mode");
        interactionMode.setValue("select mode");
        interactionMode.setNullSelectionAllowed(false);
        interactionMode.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                toggleInteractionMode((String) event.getProperty().getValue());
            }
        });
        interactionMode.setTextInputAllowed(false);
        controls.addComponent(interactionMode);
        return controls;
    }

    private void toggleInteractionMode(String mode) {
        clearInteractions();
        switch(mode){
            case "select mode" : map.addInteraction(new OLSelectInteraction());break;
            case "edit mode" : map.addInteraction(new OLModifyInteraction(vectorLayer));break;
            case "draw mode" : map.addInteraction(new OLDrawInteraction(vectorLayer, OLDrawInteractionOptions.DrawingType.LINESTRING));break;
            default: Notification.show("Unknown interaction mode : "+mode);
        }
    }

    private void clearInteractions(){
        for(OLInteraction interaction : map.getInteractions()){
            map.removeInteraction(interaction);
        }
    }

    private OLGeometry createPointFeature(double x, double y){
        return new OLPoint(x,y);
    }

    private OLFeature createRectangleFeature(String id, double x, double y, double width, double height){
        OLFeature testFeature=new OLFeature(id);
        OLLineString lineString=new OLLineString();
        lineString.add(new OLCoordinate(x,y));
        lineString.add(new OLCoordinate(x+width,y));
        lineString.add(new OLCoordinate(x+width,y+height));
        lineString.add(new OLCoordinate(x,y+height));
        lineString.add(new OLCoordinate(x,y));
        testFeature.setGeometry(lineString);
        return testFeature;
    }
}
