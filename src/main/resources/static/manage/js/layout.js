// ════════════════════════════════════════════
// LOAD COMPONENT VÀO TRANG
// ════════════════════════════════════════════

async function loadComponent(selector, url) {
  try {
    const response = await fetch(url)
    if (!response.ok) throw new Error(`Không load được ${url}`)

    const html = await response.text()
    document.querySelector(selector).innerHTML = html
  } catch (err) {
    console.error('Load component lỗi:', err)
  }
}

// ════════════════════════════════════════════
// KHỞI TẠO LAYOUT
// ════════════════════════════════════════════

async function initLayout() {
  // Load 3 component song song — nhanh hơn load tuần tự
  await Promise.all([
    loadComponent('#header-placeholder', '../components/header.html'),
    loadComponent('#navbar-placeholder', '../components/navbar.html'),
    loadComponent('#footer-placeholder', '../components/footer.html')
  ])

  // Sau khi load xong mới chạy các logic phụ thuộc DOM
  initDropdown()
  highlightActiveView()
  // initUserState()
  // highlightActiveNav()
  // initSearch()
  // initLogout()
}

// ════════════════════════════════════════════
// KIỂM TRA TRẠNG THÁI ĐĂNG NHẬP
// ════════════════════════════════════════════

function initDropdown() {

  const items = document.querySelectorAll(".menu-item.menu-toggle");
  items.forEach(item => {
    const toggle = item.querySelector(".menu-toggle")
    toggle.addEventListener("click", (e) => {
      e.preventDefault()

      const isOpen = item.classList.contains("open")
      document.querySelectorAll(".menu-item.open")
          .forEach(i => i.classList.remove("open"))

      if (!isOpen) {
        item.classList.add("open")
      }
    })
  })
}

function highlightActiveView() {
  const currentPath = window.location.pathname

  document.querySelectorAll(".menu-item.active").forEach(item => {
    item.classList.remove("active")
  })
  document.querySelectorAll(".menu-link.active").forEach(link => {
    link.classList.remove("active")
  })

  document.querySelectorAll(".menu-link").forEach(link => {
    const href = link.getAttribute("href")
    if (href && href !== "javascript:void(0);" && currentPath.includes(href)) {

      // Highlight menu-item chứa link này
      const menuItem = link.closest(".menu-item")
      if (menuItem) {
        menuItem.classList.add("active")

        // Nếu đang ở trong menu con (menu-sub), highlight cả menu cha (menu-toggle)
        const menuToggle = menuItem.closest(".menu-item.menu-toggle")
        if (menuToggle) {
          menuToggle.classList.add("active")
          menuToggle.classList.add("open")

          // Mở menu cha (expand)
          const menuSub = menuToggle.querySelector(".menu-sub")
          if (menuSub) {
            menuSub.style.display = "block"
          }
        }
      }
    }
  })
}


function initUserState() {
  const token = localStorage.getItem('accessToken')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  const guestActions = document.getElementById('guestActions')
  const userActions = document.getElementById('userActions')
  const userNameEl = document.getElementById('userName')

  if (token && user) {
    // Đã đăng nhập
    if (guestActions) guestActions.style.display = 'none'
    if (userActions) userActions.style.display = 'flex'
    if (userNameEl) userNameEl.textContent = user.fullName || user.username
  } else {
    // Chưa đăng nhập
    if (guestActions) guestActions.style.display = 'flex'
    if (userActions) userActions.style.display = 'none'
  }
}

// ════════════════════════════════════════════
// HIGHLIGHT TRANG HIỆN TẠI TRÊN NAVBAR
// ════════════════════════════════════════════

function highlightActiveNav() {
  // Lấy tên file hiện tại: /products.html → "products.html"
  const currentPage = window.location.pathname.split('/').pop() || 'index.html'

  document.querySelectorAll('.nav-link[data-page]').forEach(link => {
    const page = link.getAttribute('data-page')

    // So sánh data-page với tên file hiện tại
    if (currentPage.includes(page) && page !== '') {
      link.classList.add('active')
    }
  })
}

// ════════════════════════════════════════════
// SEARCH
// ════════════════════════════════════════════

function initSearch() {
  const searchInput = document.getElementById('searchInput')
  const searchBtn = document.getElementById('searchBtn')

  if (!searchInput || !searchBtn) return

  // Nhấn nút search
  searchBtn.addEventListener('click', () => {
    const keyword = searchInput.value.trim()
    if (keyword) {
      window.location.href = `/products.html?search=${encodeURIComponent(keyword)}`
    }
  })

  // Nhấn Enter
  searchInput.addEventListener('keypress', e => {
    if (e.key === 'Enter') searchBtn.click()
  })
}

// ════════════════════════════════════════════
// LOGOUT
// ════════════════════════════════════════════

function initLogout() {
  const logoutBtn = document.getElementById('logoutBtn')
  if (!logoutBtn) return

  logoutBtn.addEventListener('click', async () => {
    const token = localStorage.getItem('accessToken')

    try {
      // Gọi API logout để xóa refreshToken trên server
      await fetch('http://localhost:8080/api/auth/logout', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
    } catch (err) {
      console.error('Logout API lỗi:', err)
    } finally {
      // Dù API lỗi vẫn phải xóa token local
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      window.location.href = '/login.html'
    }
  })
}

// ════════════════════════════════════════════
// CHẠY KHI DOM SẴN SÀNG
// ════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', initLayout)
