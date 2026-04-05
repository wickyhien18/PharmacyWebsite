apiUrl = 'http://localhost:8080/api/medicines';

$(document).ready(function() {
    getMedicines();
});

function getMedicines() {
    $.ajax({
        url: apiUrl + '/1',
        method: 'GET',
        dataType: 'json',
        error: function(xhr, status, error) {
            console.log("XHR Status:", xhr.status);
            console.log("Error:", error);
            console.log("Response:", xhr.responseText);

            if (xhr.status === 0) {
                showError("Không thể kết nối server. Kiểm tra server đã chạy?");
            } else if (xhr.status === 404) {
                showError(`Không tìm thấy thuốc (404)`);
            } else if (xhr.status === 403) {
                showError(`Không có quyền truy cập (403)`);
            } else if (xhr.status === 500) {
                showError(`Lỗi server (500)`);
            } else {
                showError(`Lỗi ${xhr.status}: ${xhr.statusText}`);
            }
        },
        success: function (res) {
            let table = '';
            table += '<div class="col-md-5 mr-auto">';
            table += '<div class="border text-center">';
            table += '<img src="../e-commerce/images/product_07_large.png" alt="Image" class="img-fluid p-5">';
            table += '</div>';
            table += '</div>';
            table += '<div class="col-md-6">';
            table += '<h2 class="text-black">' + res.medicine_name + '</h2>';
            table += '<p>' + res.description+ '</p>';
            table += '<p><strong class="text-primary h4">' + res.price + '</strong></p>';
            table += '<div class="mb-5"> ' +
                '<div class="input-group mb-3" style="max-width: 220px;">\n' +
                '   <div class="input-group-prepend">\n' +
                '       <button class="btn btn-outline-primary js-btn-minus" type="button">&minus;</button>\n' +
                '    </div>\n' +
                '    <input type="text" class="form-control text-center" value="1" placeholder="" aria-label="Example text with button addon" aria-describedby="button-addon1">\n' +
                '    <div class="input-group-append">\n' +
                '       <button class="btn btn-outline-primary js-btn-plus" type="button">&plus;</button>\n' +
                '    </div>\n' +
                '</div>\n'
                '</div>\n';
            table += '<p><a href="cart.html" class="buy-now btn btn-sm height-auto px-4 py-3 btn-primary">Add To Cart</a></p>\n' +
                '            <div class="mt-5">\n' +
                '              <ul class="nav nav-pills mb-3 custom-pill" id="pills-tab" role="tablist">\n' +
                '                <li class="nav-item">\n' +
                '                  <a class="nav-link active" id="pills-home-tab" data-toggle="pill" href="#pills-home" role="tab"\n' +
                '                    aria-controls="pills-home" aria-selected="true">Ordering Information</a>\n' +
                '                </li>\n' +
                '                <li class="nav-item">\n' +
                '                  <a class="nav-link" id="pills-profile-tab" data-toggle="pill" href="#pills-profile" role="tab"\n' +
                '                    aria-controls="pills-profile" aria-selected="false">Specifications</a>\n' +
                '                </li>\n' +
                '            \n' +
                '              </ul>\n' +

                '            </div>\n' +
                '\n' +
                '    \n' +
                '          </div>';
            document.getElementById('medicine-detail').innerHTML = table;
        }
    })
};