import java.time.LocalDate;

public record CreateEmployeeCommand(
        String name,
        LocalDate contractStartedDate,
        LocalDate contractEndDate,
        EmployeeType type) {

}
