const path = window.location.pathname;
const medicineId = path.split('/').pop();

function getMedicineImage(imageUrl) {
    if (imageUrl && imageUrl.trim() !== '') {
        return imageUrl;
    }
    // Ảnh mặc định khi không có đường dẫn
    return '/e-commerce/images/product_07_large.png';
}

// Hàm format giá tiền
function formatPrice(price) {
    if (!price) return '0 ₫';
    return price.toLocaleString('vi-VN') + ' ₫';
}

function displayMedicine(medicine) {
    const container = document.getElementById('medicine-detail');

    if (!medicine) {
        container.innerHTML = '<p class="error">Không tìm thấy thông tin thuốc</p>';
        return;
    }

    const imageUrl = getMedicineImage(medicine.medicineImage);

    container.innerHTML = `
            <div class="col-md-5 mr-auto">
                <div class="border text-center">
                <img src="${imageUrl}" alt="${medicine.medicineName}" class="img-fluid p-5" onerror="this.src='/e-commerce/images/product_07_large.png'">
                </div>
            </div>
            
            <div class="col-md-6">
            <h2 class="text-black">${medicine.medicineName}</h2>
            <p>${medicine.description || 'Chưa có mô tả'}</p>

            <p><strong class="text-primary h4">${formatPrice(medicine.price)}</strong></p>

            <div class="mb-5">
              <div class="input-group mb-3" style="max-width: 220px;">
                <div class="input-group-prepend">
                  <button class="btn btn-outline-primary js-btn-minus" type="button">&minus;</button>
                </div>
                <input type="text" class="form-control text-center" value="1" placeholder=""
                  aria-label="Example text with button addon" aria-describedby="button-addon1">
                <div class="input-group-append">
                  <button class="btn btn-outline-primary js-btn-plus" type="button">&plus;</button>
                </div>
              </div>
    
            </div>
    `;
}

// Gọi API lấy dữ liệu
async function loadMedicineDetail() {
    try {
        const response = await fetch(`/medicines/${medicineId}`);

        if (response.ok) {
            const medicine = await response.json();
            displayMedicine(medicine);
        } else if (response.status === 404) {
            document.getElementById('medicine-detail').innerHTML = '<p class="error">Không tìm thấy thuốc</p>';
        } else {
            document.getElementById('medicine-detail').innerHTML = '<p class="error">Lỗi tải dữ liệu</p>';
        }
    } catch (error) {
        console.error('Lỗi:', error);
        document.getElementById('medicine-detail').innerHTML = '<p class="error">Lỗi kết nối server</p>';
    }
}

// Chạy khi trang load xong
document.addEventListener('DOMContentLoaded', loadMedicineDetail);