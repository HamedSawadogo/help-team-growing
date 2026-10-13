
public class EmployeeFactoryImpl implements EmployeeFactory {

    public Employee create(CreateEmployeeCommand command) {
        return switch (command.type()) {
            case CDI -> new CDIEmployee();
            case CDD -> new CDDEmployee();
        };
    }
}
