package com.mindware.ui.views.rol;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.rol.Option;
import com.mindware.backend.entity.rol.Rol;
import com.mindware.backend.rest.rol.RolRestTemplate;
//import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.GrantOptions;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import dev.mett.vaadin.tooltip.Tooltips;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Route(value = "rol-register", layout = MainLayout.class)
@PageTitle("Registro de Rol")
public class RolRegisterView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private RolRestTemplate restTemplate;

    @Autowired
    ResourceLoader resourceLoader;

    private BeanValidationBinder<Rol> binder;
    private Rol rol;

    private List<Option> optionList;
    private ObjectMapper mapper;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private ListDataProvider<Option> optionListDataProvider;

    private VerticalLayout layoutOptions;


    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();
        binder = new BeanValidationBinder<>(Rol.class);

        if(s.equals("NUEVO")){
            rol = new Rol();
            try {
                initOptions();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            setViewContent(createContent());

        }else{
            rol = restTemplate.getById(UUID.fromString(s));
            if(rol.getOptions()==null || rol.getOptions().equals("") || rol.getOptions().equals("[]")) {
                rol.setOptions("[]");
                initOptions();
            }else {
                try {
                    optionList = mapper.readValue(rol.getOptions(), new TypeReference<List<Option>>() {
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }


            setViewContent(createRol(rol));
            setViewDetails(createDetailDrawer());
            setViewDetailsPosition(Position.BOTTOM);
        }
        binder.readBean(rol);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        AppBar appBar = initAppBar();
        appBar.setTitle(Optional.ofNullable(rol.getName()).orElse("Nuevo"));

    }

    private void initOptions() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/menu.json");
        File file = File.createTempFile("menu","json");
        InputStream inputStream = resource.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream,file);
        optionList = mapper.readValue(file,new TypeReference<List<Option>>() {});
    }

    private AppBar initAppBar(){
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.addTab("PERMISOS");
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e ->{
            UI.getCurrent().navigate(RolView.class);
        });

        appBar.addTabSelectionListener(e ->{
            Tab selectedTab = MainLayout.get().getAppBar().getSelectedTab();
            if (selectedTab != null){
                if(selectedTab.getLabel().equals("PERMISOS")){
                    layoutOptions.setVisible(true);
//                    layoutStates.setVisible(false);
                }else{
                    layoutOptions.setVisible(false);
//                    layoutStates.setVisible(true);
                }
            }

        });
        appBar.centerTabs();
        return appBar;
    }

    private Component createContent(){

        FlexBoxLayout content = new FlexBoxLayout(createRol(rol));
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Horizontal.RESPONSIVE_L);
        content.setHeight("100%");
        return content;
    }

    private DetailsDrawer createDetailDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);

        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        return detailsDrawer;
    }

    private DetailsDrawer createRol(Rol rol){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("90%");
        TextField name = new TextField("Nombre Rol");
        name.setRequired(true);

        TextField description = new TextField("Descripcion del Rol");
        description.setWidthFull();

        ComboBox<String> scope = new ComboBox<>("Alcance");
        scope.setItems("LOCAL","NACIONAL");
        scope.setWidthFull();

        horizontalLayout.add(name,scope,description);

        layoutOptions = new VerticalLayout();
        layoutOptions.setHeight("100%");


        binder.forField(name).asRequired("Nombre Rol es requerido").bind(Rol::getName,Rol::setName);
        binder.forField(description).bind(Rol::getDescription,Rol::setDescription);
        binder.forField(scope).asRequired("Alcance es requerido")
                .bind(Rol::getScope,Rol::setScope);

        Grid<Option> grid = new Grid<>();
        grid.setWidthFull();
        optionListDataProvider = new ListDataProvider<>(optionList);
        grid.setDataProvider(optionListDataProvider);

        grid.addColumn(Option::getName).setFlexGrow(1).setResizable(true).setHeader("Opción");
        Grid.Column<Option> assignedColumn= grid.addColumn(new ComponentRenderer<>(this::createAssigned))
                .setHeader("Habilitar").setFlexGrow(1).setResizable(true);
        Grid.Column<Option> readColumn= grid.addColumn(new ComponentRenderer<>(this::createReader))
                .setHeader("Lectura").setFlexGrow(1).setResizable(true);
        Grid.Column<Option> writeColumn= grid.addColumn(new ComponentRenderer<>(this::createWriter))
                .setHeader("Escritura").setFlexGrow(1).setResizable(true);
        Grid.Column<Option> sendColumn = grid.addColumn(new ComponentRenderer<>(this::createSend))
                .setHeader("Enviar").setFlexGrow(1).setResizable(true);
        Grid.Column<Option> observedColumn = grid.addColumn(new ComponentRenderer<>(this::createObserved))
                .setHeader("Observar").setFlexGrow(1).setResizable(true);
        Grid.Column<Option> finishColumn = grid.addColumn(new ComponentRenderer<>(this::createFinish))
                .setHeader("Finalizar").setFlexGrow(1).setResizable(true);
        Grid.Column<Option> accountingColumn = grid.addColumn(new ComponentRenderer<>(this::createAccounting))
                .setHeader("Contabilidad").setFlexGrow(1).setResizable(true);

        Binder<Option> binderOption = new Binder<>(Option.class);
        Editor<Option> editor = grid.getEditor();
        editor.setBinder(binderOption);
        editor.setBuffered(true);

        Checkbox assigned = new Checkbox();
        binderOption.forField(assigned).bind(Option::isAssigned,Option::setAssigned);
        assignedColumn.setEditorComponent(assigned);

        Checkbox read = new Checkbox();
        binderOption.forField(read).bind(Option::isRead,Option::setRead);
        readColumn.setEditorComponent(read);

        Checkbox write = new Checkbox();
        binderOption.forField(write).bind(Option::isWrite,Option::setWrite);
        writeColumn.setEditorComponent(write);

        Checkbox send = new Checkbox();
        binderOption.forField(send).bind(Option::isSend,Option::setSend);
        sendColumn.setEditorComponent(send);

        Checkbox observed = new Checkbox();
        binderOption.forField(observed).bind(Option::isObserved,Option::setObserved);
        observedColumn.setEditorComponent(observed);

        Checkbox finish = new Checkbox();
        binderOption.forField(finish).bind(Option::isFinish,Option::setFinish);
        finishColumn.setEditorComponent(finish);

        Checkbox accounting = new Checkbox();
        binderOption.forField(accounting).bind(Option::isAccounting,Option::setAccounting);
        accountingColumn.setEditorComponent(accounting);

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Grid.Column<Option> editorColumn = grid.addComponentColumn(option -> {
            Button edit = new Button();
            Tooltips.getCurrent().setTooltip(edit,"Editar");
            edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
            edit.setIcon(VaadinIcon.EDIT.create());
            edit.setVisible(GrantOptions.grantedOptionWrite("Roles"));
            edit.addClickListener(e ->{
                editor.editItem(option);

            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Guardar", e -> editor.save());
        save.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        save.addClassName("save");

        Button cancel = new Button("Cancelar", e -> editor.cancel());
        cancel.addThemeVariants(ButtonVariant.LUMO_SMALL);
        cancel.addClassName("cancel");

        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);

        editorColumn.setEditorComponent(buttons);

        layoutOptions.add(horizontalLayout,grid);
/////////////

        footer = new DetailsDrawerFooter();
        footer.saveState(GrantOptions.grantedOptionWrite("Roles"));
        footer.addSaveListener(e ->{
            if(binder.writeBeanIfValid(rol)){
                try {
                    String options = mapper.writeValueAsString(optionList);

                    rol.setOptions(options);

                    if(rol.getId()==null) {
                        restTemplate.add(rol);
                    }else{
                        restTemplate.update(rol);
                    }
                } catch (JsonProcessingException ex) {
                    ex.printStackTrace();
                }
                UIUtils.showNotificationType("Rol Registrado","success");
                UI.getCurrent().navigate(RolView.class);
            }
        });

        footer.addCancelListener(e ->{
            UI.getCurrent().navigate(RolView.class);
        });

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setHeight("100%");
        detailsDrawer.setContent(layoutOptions);
        detailsDrawer.setFooter(footer);
        detailsDrawer.show();

        return detailsDrawer;
    }

    private Component createAssigned(Option option){
        Icon icon;
        if(option.isAssigned()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createReader(Option option){
        Icon icon;
        if(option.isRead()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createWriter(Option option){
        Icon icon;
        if(option.isWrite()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createSend(Option option){
        Icon icon;
        if(option.isSend()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createObserved(Option option){
        Icon icon;
        if(option.isObserved()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createFinish(Option option){
        Icon icon;
        if(option.isFinish()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createAccounting(Option option){
        Icon icon;
        if(option.isAccounting()){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }
}
