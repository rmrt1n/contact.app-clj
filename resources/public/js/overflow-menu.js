function overflowMenu(subtree = document) {
  document.querySelectorAll("[data-overflow-menu]").forEach((menuRoot) => {
    const btn = menuRoot.querySelector("[aria-haspopup]")
    const menu = menuRoot.querySelector("[role=menu]")
    const items = [...menuRoot.querySelectorAll("[role=menu-item]")]

    const isOpen = () => !menu.hidden
    items.forEach((i) => i.setAttribute("tabindex", "-1"))

    function toggleMenu(open = !isOpen()) {
      if (open) {
        menu.hidden = false
        btn.setAttribute("aria-expanded", "true")
        items[0].focus()
        return
      }
      menu.hidden = true
      btn.setAttribute("aria-expanded", "false")
    }

    toggleMenu(isOpen())
    btn.addEventListener("click", () => toggleMenu())
    menuRoot.addEventListener("blur", () => toggleMenu(false))

    window.addEventListener("click", function clickAway(e) {
      if (!menuRoot.isConnected) window.removeEventListener("click", clickAway)
      if (!menuRoot.contains(e.target)) toggleMenu(false)
    })

    const currentIndex = () => {
      const i = items.indexOf(document.activeElement)
      return i === -1 ? 0 : i
    }

    menu.addEventListener("keydown", (e) => {
      if (e.key === "ArrowUp") {
        items[currentIndex() - 1]?.focus();
      } else if (e.key === "ArrowDown") {
        items[currentIndex() + 1]?.focus();
      } else if (e.key === "Space") {
        items[currentIndex()].click();
      } else if (e.key === "Home") {
        items[0].focus();
      } else if (e.key === "End") {
        items[items.length - 1].focus();
      } else if (e.key === "Escape") {
        toggleMenu(false);
        btn.focus();
      }
    })
  })
}

addEventListener("htmx:load", (e) => overflowMenu(e.target))
console.log("testing")
