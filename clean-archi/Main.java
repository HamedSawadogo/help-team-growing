public class Main {

    public static void main(String[] args) {

        EmployeeFactory factory = new EmployeeFactory();

        EmployeeService employeeService = new EmployeeService(factory);

        BigDecimal cdiSalary = employeeService.calculateSalary(EmployeeType.CDI);

        BigDecimal cddSalary = employeeService.calculateSalary(EmployeeType.CDD);

        System.out.println("Salaire CDI : " + cdiSalary);
        System.out.println("Salaire CDD : " + cddSalary);
    }
}