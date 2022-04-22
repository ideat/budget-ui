package com.mindware.ui.views.config.period;

import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.entity.config.Period;
import com.mindware.backend.rest.period.PeriodRestTemplate;
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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "period", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Periodos")
public class PeriodView extends SplitViewFrame implements RouterLayout {

    @Autowired
    PeriodRestTemplate restTemplate;

    private List<Period> periodList = new ArrayList<>();

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private Button btnNew;
    private Grid<Period> grid;

    private Binder<Period> binder;
    private ListDataProvider<Period> dataProvider;

    private Period current;


    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getPeriods();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo Periodo");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
//        btnNew.setEnabled(GrantOptions.grantedOption("Parametros"));
        btnNew.addClickListener(e -> {
            showDetails(new Period());
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
        FlexBoxLayout content = new FlexBoxLayout(createGridPeriod());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }

    private void showDetails(Period period){
        current = period;
        detailsDrawerHeader.setTitle("Periodo: ".concat(period.getYear()==null?"Nuevo":period.getYear().toString()));
        detailsDrawer.setContent(createDetails(period));
        detailsDrawer.show();
        binder.readBean(period);
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
                Period result=null;
                try {
                    result = restTemplate.add(current);
                }
                catch(Exception ex){
                    UIUtils.showNotificationType(ex.getMessage(),"alert");
                    return;
                }
                if (current.getId()==null){
                    periodList.add(result);
                    grid.getDataProvider().refreshAll();
                }else{
                    grid.getDataProvider().refreshItem(current);
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

    private Grid createGridPeriod(){
        grid = new Grid<>();
        grid.setMultiSort(true);
        grid.setHeightFull();
        grid.setWidthFull();

        grid.setDataProvider(dataProvider);

        grid.addColumn(Period::getYear)
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setResizable(true)
                .setSortable(true)
                .setHeader("Gestión");
        grid.addColumn(new ComponentRenderer<>(this::createActive))
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setResizable(true)
                .setSortable(true)
                .setHeader("Activo");
        grid.addColumn(new ComponentRenderer<>(this::createButtonActivate))
                .setFlexGrow(0)
                .setAutoWidth(true)
                .setResizable(true)
                .setSortable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setAutoWidth(true)
                .setResizable(true)
                .setSortable(true);

        return grid;

    }

    private FormLayout createDetails(Period period){

        IntegerField year = new IntegerField();
        year.setWidthFull();
        year.setHasControls(true);
        year.setClearButtonVisible(true);
        year.setRequiredIndicatorVisible(true);


        binder = new BeanValidationBinder<>(Period.class);

        binder.forField(year).asRequired("Gestion es requerida")
                .withValidator(value -> value.intValue()>= LocalDate.now().getYear(),"La gestion no pude ser menor a la gestion actual ")
                .bind(Period::getYear,Period::setYear);
        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(hasChanges && isValid);
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem yearItem = form.addFormItem(year,"Gestión");
        UIUtils.setColSpan(2,yearItem);

        return form;

    }


    private Component createButtonEdit(Period period){
        Button btnEdit = new Button("Editar");
        btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnEdit.addClickListener(event -> {
            showDetails(period);
        });

        return btnEdit;
    }

    private Component createButtonActivate(Period period){
        Button btnActivate = new Button("Activar");
        btnActivate.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btnActivate.addClickListener(event -> {
            period.setIsOpen(true);
            restTemplate.add(period);

            if(periodList.size()>1) {
                Period aux = periodList.stream()
                        .filter(p -> p.getIsOpen().equals(true) && !p.getYear().equals(period.getYear()))
                        .collect(Collectors.toList()).get(0);
                if(aux != null) {
                    aux.setIsOpen(false);
                    restTemplate.add(aux);
                }

            }
            periodList.clear();
            periodList.addAll(restTemplate.getAll());
            grid.getDataProvider().refreshAll();
        });

        return btnActivate;
    }

    private Component createActive(Period period){
        Icon icon;
        if(period.getIsOpen().equals(true)){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private void getPeriods(){
        periodList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(periodList);
    }


}
