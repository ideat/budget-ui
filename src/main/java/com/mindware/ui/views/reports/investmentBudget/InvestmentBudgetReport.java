package com.mindware.ui.views.reports.investmentBudget;

import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.ViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "investment-budget-report", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Reportes")
public class InvestmentBudgetReport  extends ViewFrame implements RouterLayout {

    private static final String INVESTMENT_BUDGET_RESUME ="RESUMEN EJECUTIVO";
    private static final String INVESTMENT_BUSINESS_UNIT ="CONSOLIDADO REGIONALES";
    private static final String INVESTMENT_BUDGET_DETAIL ="DETALLE INVERSIONES";

    @Autowired
    UtilValues utilValues;

    @Autowired
    ConceptRestTemplate conceptRestTemplate;

    private List<Concept> conceptList;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        conceptList = conceptRestTemplate.getSucursal();
        Concept concept = new Concept();
        concept.setCode("20");
        concept.setCode2("50");
        concept.setDescription("OFICINA NACIONAL");
        setViewContent(createContent());
    }

    private Accordion createOptionsReport() {
        Accordion optionDetails = new Accordion();

        AccordionPanel resumeInvestmentBudget = optionDetails
                .add(INVESTMENT_BUDGET_RESUME, layoutInvestmentBusinessUnit( "investment-resume","investment-budget-report"));
        AccordionPanel investmentBusinessUnit = optionDetails
                .add(INVESTMENT_BUSINESS_UNIT, layoutInvestmentBusinessUnit("investment-business-unit","investment-budget-report"));
        AccordionPanel investmentBudgetDetail = optionDetails
                .add(INVESTMENT_BUDGET_DETAIL, layoutInvestmentBusinessUnit("investment-detail","investment-budget-report"));

        return optionDetails;
    }

    private HorizontalLayout layoutInvestmentBusinessUnit(String originReport, String pathPage){

        DatePicker cutOffDate = new DatePicker();
        cutOffDate.setWidth("300px");
        cutOffDate.setLocale(new Locale("es","BO"));
        cutOffDate.setClearButtonVisible(true);
        cutOffDate.setPlaceholder("Fecha corte");

        ComboBox<Concept> businessUnit = new ComboBox<>();
        businessUnit.setWidth("400px");
        businessUnit.setItems(conceptList);
        businessUnit.setItemLabelGenerator(Concept::getDescription);
        businessUnit.setPlaceholder("Seleccione Unidad de Negocio");

        Button print = new Button("Imprimir");
        print.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST);
        print.setIcon(VaadinIcon.PRINT.create());
        print.addClickListener(event ->{

            Map<String, List<String>> paramInvestmentBudget = new HashMap<>();
            List<String> origin = new ArrayList<>();
            origin.add(originReport);
            List<String> path = new ArrayList<>();
            path.add(pathPage);



            if(originReport.equals("investment-detail")){


                if (cutOffDate.isEmpty()) {
                    UIUtils.showNotificationType("Ingrese la Fecha de Corte", "alert");
                    cutOffDate.focus();
                    return;
                }
                if (businessUnit.isEmpty()) {
                    UIUtils.showNotificationType("Seleccione la Unidad de Negocio", "alert");
                    businessUnit.focus();
                    return;
                }

                List<String> cutOffDateParam = new ArrayList<>();
                cutOffDateParam.add(cutOffDate.getValue().toString());
                paramInvestmentBudget.put("cutoffdate",cutOffDateParam);

                List<String> year = new ArrayList<>();
                year.add(String.valueOf(cutOffDate.getValue().getYear()));

                List<String> codebusiness = new ArrayList<>();
                if(businessUnit.getValue().getDescription().equals("OFICINA NACIONAL")){
                   codebusiness.add(businessUnit.getValue().getCode2());
                }else {
                    codebusiness.add(businessUnit.getValue().getCode());
                }

                List<String> nameBusinessUnit = new ArrayList<>();
                nameBusinessUnit.add(businessUnit.getValue().getDescription());
                paramInvestmentBudget.put("codefatherbusinessunit",codebusiness);
                paramInvestmentBudget.put("year",year);
                paramInvestmentBudget.put("namebusinessunit",nameBusinessUnit);

            }else if(originReport.equals("investment-business-unit")){
                if (cutOffDate.isEmpty()) {
                    UIUtils.showNotificationType("Ingrese la Fecha de Corte", "alert");
                    cutOffDate.focus();
                    return;
                }

                List<String> cutOffDateParam = new ArrayList<>();
                cutOffDateParam.add(cutOffDate.getValue().toString());
                paramInvestmentBudget.put("cutoffdate",cutOffDateParam);

            }else if(originReport.equals("investment-resume")){
                if (cutOffDate.isEmpty()) {
                    UIUtils.showNotificationType("Ingrese la Fecha de Corte", "alert");
                    cutOffDate.focus();
                    return;
                }

                List<String> cutOffDateParam = new ArrayList<>();
                cutOffDateParam.add(cutOffDate.getValue().toString());
                paramInvestmentBudget.put("cutoffdate",cutOffDateParam);

            }
            paramInvestmentBudget.put("origin",origin);
            paramInvestmentBudget.put("path",path);

            QueryParameters qp = new QueryParameters(paramInvestmentBudget);
            UI.getCurrent().navigate("report-preview", qp);

        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        if(originReport.equals("investment-detail")){
            layout.add( cutOffDate, businessUnit,print);
        }else {
            layout.add(cutOffDate, print);
        }
        return layout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createOptionsReport());
//        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }
}
