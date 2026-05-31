//Load components of layout
async function loadComponents(selector, url) {
    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`Không load được ${url}`);

        const html = await response.text();
        document.querySelector(selector).innerHTML = html;
    } catch (err) {
        console.error("Load component lỗi:", err);
    }
}

//Load header and footer
async function loadLayout() {
    await Promise.all([
        loadComponents("#header-placeholder", "../e-commerce/html/components/header.html"),
        loadComponents("#navbar-placeholder", "../e-commerce/html/components/navbar.html"),
        loadComponents("#footer-placeholder", "../e-commerce/html/components/footer.html")
    ]);
}

document.addEventListener("DOMContentLoaded", loadLayout);
