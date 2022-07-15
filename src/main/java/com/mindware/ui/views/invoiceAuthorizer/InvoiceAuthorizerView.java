package com.mindware.ui.views.invoiceAuthorizer;

import com.mindware.backend.entity.acquisitionAuthorizer.AcquisitionAuthorizer;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.invoiceAuthorizer.InvoiceAuthorizer;
import com.mindware.backend.entity.user.UserLdapDto;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.dataLdap.DataLdapRestTemplate;
import com.mindware.backend.rest.invoiceAuthorizer.InvoiceAuthorizerRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Left;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "invoice-authorizer", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Autorizadores de Facturas")
public class InvoiceAuthorizerView extends SplitViewFrame implements RouterLayout {

    @Autowired
    private InvoiceAuthorizerRestTemplate restTemplate;

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    @Autowired
    private DataLdapRestTemplate dataLdapRestTemplate;

    @Autowired
    private UtilValues utilValues;

    private ListDataProvider<InvoiceAuthorizer> dataProvider;
    private Binder<InvoiceAuthorizer> binder;

    private InvoiceAuthorizer current;
    private List<Concept> conceptList;
    private List<UserLdapDto> userLdapDtoList;

    private TextField nameBranchOfficeFilter;
    private TextField fullNameFilter;

    private Button btnNew;
    private Grid<InvoiceAuthorizer> grid;
    private List<InvoiceAuthorizer> invoiceAuthorizerList;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        conceptList = conceptRestTemplate.getAgencia();

        getInvoiceAuthorizers();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private FlexBoxLayout createTopBar() {
        btnNew = new Button("Nuevo Autorizador");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.setVisible(GrantOptions.grantedOptionWrite("Autorizadores de Facturas"));
        btnNew.addClickListener(e -> {
            showDetails(new InvoiceAuthorizer());
        });
        FlexBoxLayout topLayout = new FlexBoxLayout();
        topLayout.setWidthFull();
        topLayout.add(btnNew);
        topLayout.setPadding(Left.L, Top.S);

        return topLayout;
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createGridInvoiceAuthorizer());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridInvoiceAuthorizer(){

        grid = new Grid();
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(InvoiceAuthorizer::getCodeBranchOffice)
                .setFlexGrow(1)
                .setKey("namebranchoffice")
                .setHeader("Unidad Negocio")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(InvoiceAuthorizer::getFullName)
                .setFlexGrow(1)
                .setKey("fullname")
                .setHeader("Autorizador")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(InvoiceAuthorizer::getPosition)
                .setFlexGrow(1)
                .setKey("position")
                .setHeader("Cargo")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setAutoWidth(true)
                .setFlexGrow(1);

        HeaderRow hr = grid.appendHeaderRow();
        nameBranchOfficeFilter = new TextField();
        nameBranchOfficeFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameBranchOfficeFilter.setWidthFull();
        nameBranchOfficeFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("namebranchoffice")).setComponent(nameBranchOfficeFilter);

        fullNameFilter = new TextField();
        fullNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        fullNameFilter.setWidthFull();
        fullNameFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("fullname")).setComponent(fullNameFilter);

        return grid;
    }

    private Component createButtonEdit(InvoiceAuthorizer invoiceAuthorizer){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(buttonClickEvent -> {
            showDetails(invoiceAuthorizer);
        });
        return btn;
    }

    private FormLayout createDetails(InvoiceAuthorizer invoiceAuthorizer){

        IntegerField codeBranchOffice = new IntegerField();
        codeBranchOffice.setReadOnly(true);
        codeBranchOffice.setWidthFull();

        TextField codePosition = new TextField();
        codePosition.setWidthFull();
        codePosition.setRequired(true);
        codePosition.setReadOnly(true);

        TextField position = new TextField();
        position.setWidthFull();
        position.setRequired(true);

        EmailField email = new EmailField();
        email.setReadOnly(true);

        ComboBox<String> fullName = new ComboBox<>();
        fullName.setWidthFull();
        fullName.setRequired(true);
        fullName.setAllowCustomValue(false);
        fullName.addValueChangeListener(event -> {
            UserLdapDto userLdapDto = userLdapDtoList.stream()
                    .filter(c -> c.getCn().equals(event.getValue()))
                    .findFirst().get();
            codePosition.setValue(userLdapDto.getTitle());
            email.setValue(userLdapDto.getEmail());

        });

        ComboBox<String> nameBranchOffice = new ComboBox<>();
        nameBranchOffice.setWidthFull();
        nameBranchOffice.setRequired(true);
        nameBranchOffice.setAllowCustomValue(false);
        nameBranchOffice.setItems(conceptList.stream()
                .map(Concept::getDescription)
                .collect(Collectors.toList()));
        nameBranchOffice.addValueChangeListener(event -> {
            String code2 = conceptList.stream()
                    .filter(c -> c.getDescription().equals(event.getValue()))
                    .map(Concept::getCode2)
                    .findFirst().get();
            codeBranchOffice.setValue(Integer.valueOf(code2));
            userLdapDtoList = dataLdapRestTemplate.getByCodeBusinessUnit(Integer.valueOf(code2));
            fullName.clear();
            fullName.setItems(userLdapDtoList.stream()
                    .map(UserLdapDto::getCn)
                    .collect(Collectors.toList()));
        });

        RadioButtonGroup<String> state = new RadioButtonGroup<>();
        state.setItems("ACTIVO","BAJA");
        state.setRequired(true);

        ComboBox<String> priorityLevel = new ComboBox<>();
        priorityLevel.setWidthFull();
        priorityLevel.setAllowCustomValue(false);
        priorityLevel.setItems(utilValues.getValueParameterByCategory("NIVELES AUTORIZACION"));

        binder = new BeanValidationBinder<>(InvoiceAuthorizer.class);
        binder.forField(codeBranchOffice)
                .asRequired("Código Unidad Negocio es requerido")
                .bind(InvoiceAuthorizer::getCodeBranchOffice,InvoiceAuthorizer::setCodeBranchOffice);
        binder.forField(nameBranchOffice)
                .asRequired("Nombre Unidad Negocio es requerido")
                .bind(InvoiceAuthorizer::getNameBranchOffice, InvoiceAuthorizer::setNameBranchOffice);
        binder.forField(codePosition)
                .asRequired("Código Cargo es Requerido")
                .bind(InvoiceAuthorizer::getCodePosition, InvoiceAuthorizer::setCodePosition);
        binder.forField(position)
                .asRequired("Nombre Cargo es Requerido")
                .bind(InvoiceAuthorizer::getPosition,InvoiceAuthorizer::setPosition);
        binder.forField(fullName)
                .asRequired("Nombre Autorizador es requerido")
                .bind(InvoiceAuthorizer::getFullName,InvoiceAuthorizer::setFullName);
        binder.forField(state)
                .asRequired("Estado del Autorizador es requerido")
                .bind(InvoiceAuthorizer::getState,InvoiceAuthorizer::setState);
        binder.forField(email)
                .asRequired("Correo Autorizador es requerido")
                .bind(InvoiceAuthorizer::getEmail,InvoiceAuthorizer::setEmail);
        binder.forField(priorityLevel)
                .asRequired("Nivel Autorización es requerido")
                .bind(InvoiceAuthorizer::getPriorityLevel, InvoiceAuthorizer::setPriorityLevel);

        binder.addStatusChangeListener(event ->{
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Autorizadores de Facturas"));
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem nameBranchOfficeItem = form.addFormItem(nameBranchOffice,"Unidad Negocio");
        UIUtils.setColSpan(2,nameBranchOfficeItem);
        FormLayout.FormItem fullNameItem = form.addFormItem(fullName,"Nombre Autorizador");
        UIUtils.setColSpan(2,fullNameItem);
        form.addFormItem(codeBranchOffice,"Código Unidad Negocio");
        form.addFormItem(codePosition,"Código Cargo");
        FormLayout.FormItem positionItem =  form.addFormItem(position,"Nombre Cargo");
        UIUtils.setColSpan(2,positionItem);
        form.addFormItem(email,"Correo Empleado");
        form.addFormItem(state,"Estado Autorizador");
        form.addFormItem(priorityLevel,"Nivel Autorización");

        return form;
    }

    private void showDetails(InvoiceAuthorizer invoiceAuthorizer){
        current = invoiceAuthorizer;
        detailsDrawer.setContent(createDetails(current));
        detailsDrawerHeader.setTitle("Autorizador: ".concat(current.getFullName()==null?"Nuevo":current.getFullName()));
        detailsDrawer.show();
        binder.readBean(current);
    }

    private DetailsDrawer createDetailsDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        footer = new DetailsDrawerFooter();
        footer.addSaveListener(e ->{
            if (current !=null && binder.writeBeanIfValid(current)){
                InvoiceAuthorizer result= new InvoiceAuthorizer();
                try {
                    result = restTemplate.add(current);
                }catch (Exception ex){
                    UIUtils.showNotificationType(ex.getMessage(),"alert");
                    return;
                }
                if(current.getId()==null){
                    invoiceAuthorizerList.add(result);
                    grid.getDataProvider().refreshAll();
                }else{
                    grid.getDataProvider().refreshAll();
                }
                detailsDrawer.hide();
            }else{
                UIUtils.dialog("Datos incorrectos, verifique nuevamente","alert").open();
            }
        });
        footer.addCancelListener(e ->{
            footer.saveState(false);
            detailsDrawer.hide();
        });

        detailsDrawer.setFooter(footer);
        return detailsDrawer;
    }

    public void getInvoiceAuthorizers(){
        invoiceAuthorizerList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(invoiceAuthorizerList);
    }

    private void applyFilter(ListDataProvider<InvoiceAuthorizer> dataProvider){
        dataProvider.clearFilters();
        if(!nameBranchOfficeFilter.getValue().trim().equals("")){
            dataProvider.addFilter(invoiceAuthorizer -> StringUtils.containsIgnoreCase(invoiceAuthorizer.getNameBranchOffice(),nameBranchOfficeFilter.getValue()));
        }
        if(!fullNameFilter.getValue().trim().equals("")){
            dataProvider.addFilter(invoiceAuthorizer -> StringUtils.containsIgnoreCase(invoiceAuthorizer.getFullName(),fullNameFilter.getValue()));
        }
    }
}
