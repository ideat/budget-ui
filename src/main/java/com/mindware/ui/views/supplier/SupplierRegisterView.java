package com.mindware.ui.views.supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.supplier.ShareHolder;
import com.mindware.backend.entity.supplier.Supplier;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.DialogSweetAlert;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.Left;
import com.mindware.ui.layout.size.Right;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.*;
import com.wontlost.sweetalert2.SweetAlert2Vaadin;
import dev.mett.vaadin.tooltip.Tooltips;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Route(value = "supplier-register", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Registro Proveedor")
public class SupplierRegisterView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private SupplierRestTemplate restTemplate;

    @Autowired
    private UtilValues utilValues;

    private Binder<Supplier> binder;
    private Binder<ShareHolder> shareHolderBinder;

    private Supplier supplier;
    private List<ShareHolder> shareHolderList;
    private ShareHolder currentShareHolder;
    private ShareHolder initShareHolder;

    private ObjectMapper mapper;

    private ListDataProvider<ShareHolder> shareHolderListDataProvider;
    private ListDataProvider<Supplier> dataProvider;

    private DetailsDrawerFooter footerShareHolder;
    private DetailsDrawerFooter footerSupplier;
    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private String title;

    private Grid<ShareHolder> gridShareHolder;


    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();

        if(!s.contains("NUEVO")){
            supplier = restTemplate.getById(s);
            title = "Proveedor: ".concat(supplier.getName());
        }else{
            supplier = new Supplier();
            supplier.setShareHolders("[]");
            title = "Registro Nuevo Proveedor";
        }

        try {
            shareHolderList = mapper.readValue(supplier.getShareHolders(), new TypeReference<List<ShareHolder>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        shareHolderListDataProvider = new ListDataProvider<>(shareHolderList);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        initBar();
        setViewContent(createSupplierForm(supplier));
        setViewDetails(createDetailsDrawer());
    }

    private AppBar initBar(){
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.setTitle(title);
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate("supplier"));

        return appBar;
    }

    private DetailsDrawer createSupplierForm(Supplier suppplier){

        TextField nit = new TextField();
        nit.setWidthFull();
        nit.setRequiredIndicatorVisible(true);

        TextField idCardLegalRepresentative = new TextField();
        idCardLegalRepresentative.setWidthFull();
        idCardLegalRepresentative.setRequired(true);
        idCardLegalRepresentative.setRequiredIndicatorVisible(true);
        idCardLegalRepresentative.getElement().getThemeList().add("upper-textfield");

        TextField name = new TextField();
        name.setWidthFull();
        name.setRequired(true);

        TextField legalRepresentative = new TextField();
        legalRepresentative.setWidthFull();
        legalRepresentative.setRequired(true);

        TextField areaWork = new TextField();
        areaWork.setWidthFull();
        areaWork.setRequired(true);

        ComboBox<String> typeBusinessCompany = new ComboBox<>();
        typeBusinessCompany.setWidthFull();
        typeBusinessCompany.setRequired(true);
        typeBusinessCompany.setItems(utilValues.getValueParameterByCategory("TIPO SOCIEDAD"));

        TextField primaryActivity = new TextField();
        primaryActivity.setWidthFull();
        primaryActivity.setRequired(true);

        EmailField email = new EmailField();
        email.setClearButtonVisible(true);
        email.setErrorMessage("Correo Invalido");
        email.getElement().setAttribute("name", "email");
        email.setWidthFull();

        TextField address = new TextField();
        address.setWidthFull();

        TextField phoneNumber = new TextField();
        phoneNumber.setWidthFull();

        ComboBox<String> location = new ComboBox<>();
        location.setWidthFull();
        location.setRequired(true);
        location.setItems(utilValues.getValueParameterByCategory("OFICINAS"));

        RadioButtonGroup<String> pendingCompleting = new RadioButtonGroup<>();
        pendingCompleting.setItems("SI","NO");
        pendingCompleting.setRequired(true);

        binder = new BeanValidationBinder<>(Supplier.class);
        binder.forField(nit)
                .asRequired("NIT es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Supplier::getNit,Supplier::setNit);
        binder.forField(name)
                .asRequired("Nombre o Razón Social es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Supplier::getName, Supplier::setName);
        binder.forField(idCardLegalRepresentative)
                .asRequired("Carnet Identidad Representante Legal es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Supplier::getIdCardLegalRepresentative,Supplier::setIdCardLegalRepresentative);
        binder.forField(legalRepresentative)
                .asRequired("Nombre Representante Legal es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Supplier::getLegalRepresentative,Supplier::setLegalRepresentative);
        binder.forField(areaWork)
                .asRequired("Rubro es requerido")
                .bind(Supplier::getAreaWork,Supplier::setAreaWork);
        binder.forField(typeBusinessCompany)
                .asRequired("Tipo Sociedad es requerido")
                .bind(Supplier::getTypeBusinessCompany,Supplier::setTypeBusinessCompany);
        binder.forField(primaryActivity)
                .asRequired("Actividad Principal es requerida")
                .bind(Supplier::getPrimaryActivity,Supplier::setPrimaryActivity);
        binder.forField(email)

                .bind(Supplier::getEmail,Supplier::setEmail);
        binder.forField(address)
                .bind(Supplier::getAddress,Supplier::setAddress);
        binder.forField(phoneNumber)
                .bind(Supplier::getPhoneNumber,Supplier::setPhoneNumber);
        binder.forField(location)
                .asRequired("Ubicación Oficina es requerida")
                .bind(Supplier::getLocation,Supplier::setLocation);
        binder.forField(pendingCompleting)
                .asRequired("Indicar el estado de los datos es requerido")
                .bind(Supplier::getPendingCompleting,Supplier::setPendingCompleting);

        binder.readBean(supplier);

        binder.addStatusChangeListener(event -> {
           boolean isValid = !event.hasValidationErrors();
           boolean hasChanges = binder.hasChanges();
           footerSupplier.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Proveedores"));
//           footerShareHolder.saveState(isValid && hasChanges && GrantOptions.grantedOption("Proveedores"));
        });

        FormLayout form = new FormLayout();
        form.setSizeUndefined();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px",2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("810px",3,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px",4,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );

        form.addFormItem(nit,"Nro. de NIT");
        form.addFormItem(name,"Nombre o Razón Social");
        form.addFormItem(idCardLegalRepresentative,"Carnet Identidad Representante Legal");
        form.addFormItem(legalRepresentative,"Representante Legal");
        form.addFormItem(typeBusinessCompany,"Tipo Sociedad");
        form.addFormItem(areaWork,"Rubro");
        form.addFormItem(primaryActivity,"Actividad Principal");
        form.addFormItem(email,"Correo Electrónico");
        form.addFormItem(address,"Dirección Negocio");
        form.addFormItem(phoneNumber,"Teléfonos");
        form.addFormItem(location,"Oficina");
        form.addFormItem(pendingCompleting,"¿Información completa?");

//        footerShareHolder = new DetailsDrawerFooter();
        footerSupplier = new DetailsDrawerFooter();
        footerSupplier.saveState(GrantOptions.grantedOptionWrite("Proveedores"));

        footerSupplier.addSaveListener(event -> {

            if(!email.isEmpty()){
                binder.forField(email)
                        .withValidator(new EmailValidator("Correo Electrónico Inválido"))
                        .bind(Supplier::getEmail,Supplier::setEmail);
//                if(email.isInvalid()){
//                    email.setInvalid(true);
//                    return;
//                }
            }else{
                binder.removeBinding(email);
                binder.forField(email)
                        .bind(Supplier::getEmail,Supplier::setEmail);
            }
            if(binder.writeBeanIfValid(suppplier)){
                try {
                    String jsonShareHolders = mapper.writeValueAsString(shareHolderList);
                    suppplier.setShareHolders(jsonShareHolders);
                    supplier.setIdCardLegalRepresentative(idCardLegalRepresentative.getValue().toUpperCase());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                try {
                    restTemplate.add(suppplier);
                    UIUtils.showNotificationType("Proveedor registrado","success");
                    UI.getCurrent().navigate(SupplierView.class);
                }catch(Exception e){
                    String[] arrMsg = e.getMessage().split(",");
                    String[] msg = arrMsg[1].split(":");
                    UIUtils.showNotificationType(msg[1].replaceAll("\"",""),"alert");
                }

            }
        });

        footerSupplier.addCancelListener(e -> {
            UI.getCurrent().navigate("supplier");
        });

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setHeight("100%");
        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(form,layoutGridShareHolders());
        detailsDrawer.setFooter(footerSupplier);
        detailsDrawer.show();


        return detailsDrawer;

    }

    private VerticalLayout layoutGridShareHolders(){
        VerticalLayout layout = new VerticalLayout();
        gridShareHolder = new Grid<>();
        gridShareHolder.setDataProvider(shareHolderListDataProvider);
        gridShareHolder.setWidthFull();

//        gridShareHolder.addSelectionListener(event -> {
//            initShareHolder = event.getFirstSelectedItem().get();
//            event.getFirstSelectedItem().ifPresent(this::showDetails);
//        });

        gridShareHolder.addColumn(ShareHolder::getIdCard)
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setSortable(true)
                .setHeader("Número de documento de identidad");
        gridShareHolder.addColumn(ShareHolder::getFullName)
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setSortable(true)
                .setHeader("Nombre Completo");
        gridShareHolder.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.START);
        gridShareHolder.addColumn(new ComponentRenderer<>(this::createButtonDelete))
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.START);

        Button btnNewShareHolder = new Button("Nuevo Accionista");
        btnNewShareHolder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNewShareHolder.addClickListener(e -> {
            showDetails(new ShareHolder());
        });

        HorizontalLayout headerLayout = new HorizontalLayout();

        headerLayout.add(btnNewShareHolder);

        layout.add(headerLayout,gridShareHolder);

        return layout;
    }

    private Component createButtonEdit(ShareHolder shareHolder){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());

        btn.addClickListener(buttonClickEvent -> {
            showDetails(shareHolder);
        });

        return btn;
    }

    private Component createButtonDelete(ShareHolder shareHolder){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {

            SweetAlert2Vaadin sweetAlert2Vaadin = new DialogSweetAlert().dialogConfirm("Eliminar Registro",
                    "¿Deseas Eliminar al Accionista? ");

            sweetAlert2Vaadin.open();
            sweetAlert2Vaadin.addConfirmListener(e -> {
                shareHolderList.remove(shareHolder);
                shareHolderListDataProvider.refreshAll();
                footerSupplier.saveState(GrantOptions.grantedOptionWrite("Proveedores"));
            });
            sweetAlert2Vaadin.addCancelListener(e -> e.getSource().close());

        });

        return btn;
    }

    private void showDetails(ShareHolder shareHolder){
        currentShareHolder = shareHolder;
        detailsDrawerHeader.setTitle("Accionista: ".concat(shareHolder.getFullName()==null?"Nuevo":shareHolder.getFullName()));
        detailsDrawer.setContent(createDetails(shareHolder));
        detailsDrawer.show();
        shareHolderBinder.readBean(shareHolder);
    }

    private DetailsDrawer createDetailsDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        footerShareHolder = new DetailsDrawerFooter();
        footerShareHolder.addSaveListener(e ->{
            if (currentShareHolder !=null && shareHolderBinder.writeBeanIfValid(currentShareHolder)){

                if (currentShareHolder.getId()==null){
                    currentShareHolder.setId(UUID.randomUUID());
                }else{
                    shareHolderList.remove(currentShareHolder);
                }
                shareHolderList.add(currentShareHolder);
                gridShareHolder.getDataProvider().refreshAll();
                footerSupplier.saveState(true);
                detailsDrawer.hide();
            }else{
                UIUtils.showNotificationType("Datos incorrectos, verifique nuevamente","alert");
            }
        });

        footerShareHolder.addCancelListener(e ->{

            detailsDrawer.hide();

        });

        detailsDrawer.setFooter(footerShareHolder);
        return detailsDrawer;
    }

    private FormLayout createDetails(ShareHolder shareHolder){

        TextField idCard = new TextField();
        idCard.setWidthFull();
        idCard.setRequired(true);

        TextField fullName = new TextField();
        fullName.setWidthFull();
        fullName.setRequired(true);

        shareHolderBinder = new BeanValidationBinder<>(ShareHolder.class);
        shareHolderBinder.forField(idCard)
                .asRequired("Número de documento de identidad es requerido")
                .bind(ShareHolder::getIdCard,ShareHolder::setIdCard);
        shareHolderBinder.forField(fullName)
                .asRequired("Nombre Completo es requerido")
                .bind(ShareHolder::getFullName,ShareHolder::setFullName);
        shareHolderBinder.addStatusChangeListener(event -> {
                boolean isValid = !event.hasValidationErrors();
                boolean hasChanges = shareHolderBinder.hasChanges();
                footerShareHolder.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Proveedores"));
        });
        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem idCardItem = formLayout.addFormItem(idCard,"Número de documento de identidad");
        FormLayout.FormItem fullNameItem = formLayout.addFormItem(fullName,"Nombre Completo");
        UIUtils.setColSpan(2,idCardItem);
        UIUtils.setColSpan(2,fullNameItem);

        return formLayout;
    }
}
