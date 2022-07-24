package com.mindware.ui.views.config.typeChangeCurrency;

import com.mindware.backend.entity.config.TypeChangeCurrency;
import com.mindware.backend.rest.typeChangeCurrency.TypeChangeCurrencyRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.layout.size.Horizontal;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import dev.mett.vaadin.tooltip.Tooltips;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "typechangecurrency", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Tipo Cambio")
public class TypeChangeCurrencyView extends SplitViewFrame implements RouterLayout {

    @Autowired
    private TypeChangeCurrencyRestTemplate restTemplate;

    @Autowired
    private UtilValues utilValues;

    private List<TypeChangeCurrency> typeChangeCurrencyList= new ArrayList<>();
    private ListDataProvider<TypeChangeCurrency> dataProvider;
    private Binder<TypeChangeCurrency> binder;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private Button btnNew;
    private Grid<TypeChangeCurrency> grid;
    private TypeChangeCurrency current;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getListTypeChangeCurrency();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.setVisible(GrantOptions.grantedOptionWrite("Tipo Cambio"));
        btnNew.addClickListener(e -> {
            showDetails(new TypeChangeCurrency());
        });

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth("100%");
        topLayout.add(btnNew);
        topLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END,btnNew);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);

        return topLayout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createGridTypeChangeCurrency());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }

    private void showDetails(TypeChangeCurrency typeChangeCurrency){
        current = typeChangeCurrency;
        detailsDrawerHeader.setTitle("Tipo Cambio: ".concat(current.getName()==null?"Nuevo":current.getName()));
        detailsDrawer.setContent(createDetails(typeChangeCurrency));
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
                try {
                    TypeChangeCurrency result = restTemplate.add(current);
                    if (current.getId() == null) {
                        typeChangeCurrencyList.add(result);
                        grid.getDataProvider().refreshAll();
                    } else {
                        grid.getDataProvider().refreshItem(current);
                    }
                    detailsDrawer.hide();
                }catch (Exception ex){
                    String[] re = ex.getMessage().split(",");
                    String[] msg = re[1].split(":");
                    UIUtils.showNotificationType(msg[1],"alert");
                    return;
                }
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

    private Grid createGridTypeChangeCurrency(){

        grid = new Grid<>();
        grid.setMultiSort(true);
        grid.setSizeFull();

        grid.setDataProvider(dataProvider);

        grid.addColumn(TypeChangeCurrency::getName)
                .setFlexGrow(1)
                .setResizable(true)
                .setAutoWidth(true)
                .setHeader("Nombre");
        grid.addColumn(TypeChangeCurrency::getCurrency)
                .setFlexGrow(1)
                .setResizable(true)
                .setAutoWidth(true)
                .setHeader("Moneda");
        grid.addColumn(TypeChangeCurrency::getAmountChange)
                .setFlexGrow(1)
                .setResizable(true)
                .setAutoWidth(true)
                .setHeader("Tipo Cambio");
        grid.addColumn(new LocalDateRenderer<>(TypeChangeCurrency::getValidityStart, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFlexGrow(1)
                .setResizable(true)
                .setAutoWidth(true)
                .setHeader("Fecha Vigencia");
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setAutoWidth(true)
                .setFlexGrow(0);

        return grid;
    }

    private Component createButtonEdit(TypeChangeCurrency typeChangeCurrency){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(event -> {
            showDetails(typeChangeCurrency);
        });
        return btn;
    }

    private FormLayout createDetails(TypeChangeCurrency typeChangeCurrency){

        ComboBox<String> name = new ComboBox<>();
        name.setRequired(true);
        name.setWidthFull();
        name.setItems(utilValues.getValueParameterByCategory("CATEGORIA TIPO CAMBIO"));
        name.setAllowCustomValue(false);
        name.setAutoOpen(true);

        ComboBox<String> currency = new ComboBox<>();
        currency.setWidthFull();
        currency.setAllowCustomValue(false);
        currency.setAutoOpen(true);
        currency.setItems(utilValues.getValueParameterByCategory("MONEDA"));

        NumberField amountChange = new NumberField();
        amountChange.setWidthFull();
        amountChange.setMin(0.0);
        amountChange.setClearButtonVisible(true);

        DatePicker validityStart = new DatePicker();
        validityStart.setRequired(true);
        validityStart.setLocale(new Locale("es","BO"));

        binder = new BeanValidationBinder<>(TypeChangeCurrency.class);
        binder.forField(name)
                .asRequired("Nombre Tipo Cambio es requerido")
                .bind(TypeChangeCurrency::getName,TypeChangeCurrency::setName);
        binder.forField(currency)
                .asRequired("Moneda es requerida")
                .bind(TypeChangeCurrency::getCurrency,TypeChangeCurrency::setCurrency);
        binder.forField(amountChange)
                .asRequired("Tipo de Cambio es requerido")
                .withValidator(a -> a.doubleValue()>0.0,"Tipo Cambio debe ser mayor a 0")
                .bind(TypeChangeCurrency::getAmountChange,TypeChangeCurrency::setAmountChange);
        binder.forField(validityStart)
                .asRequired("Fecha Vigencia es requerida")
                .bind(TypeChangeCurrency::getValidityStart,TypeChangeCurrency::setValidityStart);

        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(hasChanges && isValid && GrantOptions.grantedOptionWrite("Tipo Cambio"));
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem nameItem = form.addFormItem(name,"Nombre Tipo Cambio");
        UIUtils.setColSpan(2,nameItem);
        form.addFormItem(currency,"Moneda");
        form.addFormItem(amountChange,"Tipo Cambio");
        form.addFormItem(validityStart,"Fecha Vigencia");

        return form;
    }

    private void getListTypeChangeCurrency(){
        typeChangeCurrencyList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider(typeChangeCurrencyList);
    }
}
