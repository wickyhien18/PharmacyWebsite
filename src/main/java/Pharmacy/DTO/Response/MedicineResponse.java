package Pharmacy.DTO.Response;

public record MedicineResponse(
//        Integer medicineId,
        String medicineName,
        String categoryName,
        String manufacturerName,
        Float price,
        Integer quantity
) {
}
