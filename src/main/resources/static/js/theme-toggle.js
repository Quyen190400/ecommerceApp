// theme-toggle.js
(function() {
    function setTheme(theme) {
        console.log('[theme-toggle] setTheme:', theme);
        document.body.classList.toggle('dark-theme', theme === 'dark');
        localStorage.setItem('theme', theme);
        // Đổi icon/text cho cả desktop và mobile
        var btns = [
            document.getElementById('themeToggleBtn'),
            document.getElementById('mobileThemeToggleBtn')
        ];
        btns.forEach(function(btn) {
            if (btn) {
                var icon = btn.querySelector('i');
                var text = btn.querySelector('span');
                if (icon && text) {
                    if (theme === 'dark') {
                        text.textContent = '☀️ Chế độ sáng';
                    } else {
                        text.textContent = '🌙 Chế độ tối';
                    }
                }
            }
        });
    }
    function toggleTheme() {
        var isDark = document.body.classList.contains('dark-theme');
        var newTheme = isDark ? 'light' : 'dark';
        console.log('[theme-toggle] toggleTheme, newTheme:', newTheme);
        setTheme(newTheme);
    }
    document.addEventListener('DOMContentLoaded', function() {
        var savedTheme = localStorage.getItem('theme') || 'light';
        console.log('[theme-toggle] DOMContentLoaded, savedTheme:', savedTheme);
        setTheme(savedTheme);
        var btns = [
            document.getElementById('themeToggleBtn'),
            document.getElementById('mobileThemeToggleBtn')
        ];
        btns.forEach(function(btn) {
            if (btn) {
                btn.onclick = toggleTheme;
                console.log('[theme-toggle] Gán onclick cho', btn.id);
            }
        });
    });
})(); 