package com.mindware.ui.views.acquisitionAuthorizer;

import com.mindware.backend.entity.acquisitionAuthorizer.AcquisitionAuthorizer;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.user.UserLdapDto;
import com.mindware.backend.rest.acquisitionAuthorizer.AcquisitionAuthorizerRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.dataLdap.DataLdapRestTemplate;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
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
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "acquisition-authorizer", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Autorizadores de Adquisiciones")
public class AcquisitionAuthorizerView  extends SplitViewFrame implements RouterLayout {

    @Autowired
    private AcquisitionAuthorizerRestTemplate restTemplate;

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    @Autowired
    private DataLdapRestTemplate dataLdapRestTemplate;

    private ListDataProvider<AcquisitionAuthorizer> dataProvider;
    private Binder<AcquisitionAuthorizer> binder;

    private AcquisitionAuthorizer current;
    private List<Concept> conceptList;
    private List<UserLdapDto> userLdapDtoList;

    private TextField nameBranchOfficeFilter;
    private TextField fullNameFilter;

    private Button btnNew;
    private Grid<AcquisitionAuthorizer> grid;
    private List<AcquisitionAuthorizer> acquisitionAuthorizerList;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);

        conceptList = conceptRestTemplate.getAgencia();

        getAcquisitionAuthorizers();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private FlexBoxLayout createTopBar() {
        btnNew = new Button("Nuevo Autorizador");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
            showDetails(new AcquisitionAuthorizer());
        });
        FlexBoxLayout topLayout = new FlexBoxLayout();
        topLayout.setWidthFull();
        topLayout.add(btnNew);
        topLayout.setPadding(Left.L, Top.S);

        return topLayout;
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createGridAcquisitionAuthorizer());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridAcquisitionAuthorizer(){

        grid = new Grid();
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(AcquisitionAuthorizer::getNameBranchOffice)
                .setFlexGrow(1)
                .setKey("namebranchoffice")
                .setHeader("Unidad Negocio")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(AcquisitionAuthorizer::getFullName)
                .setFlexGrow(1)
                .setKey("fullname")
                .setHeader("Autorizador")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(AcquisitionAuthorizer::getPosition)
                .setFlexGrow(1)
                .setKey("position")
                .setHeader("Cargo")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new NumberRenderer<>(AcquisitionAuthorizer::getMaxAmount,
                " %(,.2f",
                Locale.US, "0.00"))
                .setFlexGrow(1)
                .setHeader("Monto $us")
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

    private Component createButtonEdit(AcquisitionAuthorizer acquisitionAuthorizer){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(buttonClickEvent -> {
            showDetails(acquisitionAuthorizer);
        });
        return btn;
    }

    private FormLayout createDetails(AcquisitionAuthorizer acquisitionAuthorizer){

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
        email.setRequiredIndicatorVisible(true);
        email.setReadOnly(true);
        email.setWidthFull();

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

        NumberField maxAmount = new NumberField();
        maxAmount.setWidthFull();
        maxAmount.setRequiredIndicatorVisible(true);
        maxAmount.setClearButtonVisible(true);
        maxAmount.setMin(0.0);

        RadioButtonGroup<String> state = new RadioButtonGroup<>();
        state.setItems("ACTIVO","BAJA");
        state.setRequired(true);

        binder = new BeanValidationBinder<>(AcquisitionAuthorizer.class);
        binder.forField(codeBranchOffice)
                .asRequired("Codigo Unidad Negocio es requerido")
                .bind(AcquisitionAuthorizer::getCodeBranchOffice,AcquisitionAuthorizer::setCodeBranchOffice);
        binder.forField(nameBranchOffice)
                .asRequired("Nombre Unidad Negocio es requerido")
                .bind(AcquisitionAuthorizer::getNameBranchOffice, AcquisitionAuthorizer::setNameBranchOffice);
        binder.forField(codePosition)
                .asRequired("Codigo Cargo es Requerido")
                .bind(AcquisitionAuthorizer::getCodePosition, AcquisitionAuthorizer::setCodePosition);
        binder.forField(position)
                .asRequired("Nombre Cargo es Requerido")
                .bind(AcquisitionAuthorizer::getPosition,AcquisitionAuthorizer::setPosition);
        binder.forField(fullName)
                .asRequired("Nombre funcionario es requerido")
                .bind(AcquisitionAuthorizer::getFullName,AcquisitionAuthorizer::setFullName);
        binder.forField(state)
                .asRequired("Estado del Autorizador es requerido")
                .bind(AcquisitionAuthorizer::getState,AcquisitionAuthorizer::setState);
        binder.forField(maxAmount)
                .asRequired("Monto maximo es requerido")
                .withValidator(m -> m.doubleValue()>0.0,"Monto debe ser mayor a 0")
                .bind(AcquisitionAuthorizer::getMaxAmount,AcquisitionAuthorizer::setMaxAmount);
        binder.forField(email)
                .asRequired("Correo Autorizador es requerido")
                .bind(AcquisitionAuthorizer::getEmail,AcquisitionAuthorizer::setEmail);
        binder.addStatusChangeListener(event ->{
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(isValid && hasChanges);
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
        form.addFormItem(codeBranchOffice,"Codigo Unidad Negocio");
        form.addFormItem(codePosition,"Codigo Cargo");
        FormLayout.FormItem positionItem =  form.addFormItem(position,"Nombre Cargo");
        UIUtils.setColSpan(2,positionItem);
        FormLayout.FormItem emailItem = form.addFormItem(email,"Correo Autorizador");
        UIUtils.setColSpan(2,emailItem);
        form.addFormItem(maxAmount,"Monto $us");
        form.addFormItem(state,"Estado Autorizador");

        return form;

    }

    private void showDetails(AcquisitionAuthorizer acquisitionAuthorizer){
        current = acquisitionAuthorizer;
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
                AcquisitionAuthorizer result = restTemplate.add(current);
                if(current.getId()==null){
                    acquisitionAuthorizerList.add(result);
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

    public void getAcquisitionAuthorizers() {
        acquisitionAuthorizerList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(acquisitionAuthorizerList);

    }

    private void applyFilter(ListDataProvider<AcquisitionAuthorizer> dataProvider){
        dataProvider.clearFilters();
        if(!nameBranchOfficeFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionAuthorizer -> StringUtils.containsIgnoreCase(acquisitionAuthorizer.getNameBranchOffice(),nameBranchOfficeFilter.getValue()));
        }
        if(!fullNameFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionAuthorizer -> StringUtils.containsIgnoreCase(acquisitionAuthorizer.getFullName(),fullNameFilter.getValue()));
        }
    }
}
