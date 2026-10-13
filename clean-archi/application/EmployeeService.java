import java.math.BigDecimal;

class EmployeeService {

    private final EmployeeFactory employeeFactory;

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeFactory employeeFactory,
            EmployeeRepository employeeRepository) {
        this.employeeFactory = employeeFactory;
        this.employeeRepository = employeeRepository;
    }

    public BigDecimal calculateSalary(EmployeeType type) {
        Employee employee = employeeFactory.create(type);
        employeeRepository.save(employee);
        return employee.calculateSalary();
    }
}