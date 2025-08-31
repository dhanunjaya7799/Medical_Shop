package com.example.Medicine_Bill;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<CartItem> cart = new ArrayList<>();

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("cart", cart);
        if (!cart.isEmpty()) {
            model.addAttribute("date", LocalDate.now());
            model.addAttribute("totalQty", cart.stream().mapToInt(CartItem::getQuantity).sum());
            model.addAttribute("subtotal", cart.stream().mapToDouble(CartItem::getTotal).sum());
        }
        return "index";
    }
    @GetMapping("/adddb")
    public String adddb() {
    	return "home";
    }
    @PostMapping("/saveMedicine")
    public String saveMedicine(@RequestParam String name,
                               @RequestParam double price,
                               @RequestParam double discount_rate,
                               @RequestParam String expiry_date) {

        String sql = "INSERT INTO medicines (name, price, discount_rate, expiry_date) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, name, price, discount_rate, java.sql.Date.valueOf(expiry_date));

        return "redirect:/"; // redirect to home page after saving
    }


    @PostMapping("/addMedicine")
    public String addMedicine(@RequestParam("medicineName") String medicineName,
                              @RequestParam("quantity") int quantity) {
        try {
            // ✅ Fetch medicine details from DB
            String sql = "SELECT price, expiry_date FROM medicines WHERE name = ?";
            Map<String, Object> medicine = jdbcTemplate.queryForMap(sql, medicineName);

            double price = ((Number) medicine.get("price")).doubleValue();
            double discountRate = (quantity >= 10) ? 0.10 : 0.0; // 10% discount
            double total = price * quantity;
            double discountAmount = total * discountRate;

            // ✅ Add to cart
            CartItem item = new CartItem(medicineName, quantity, price, discountAmount);
            cart.add(item);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ Redirect (PRG pattern) → prevents re-adding on refresh
        return "redirect:/";
    }

    @GetMapping("/delete/{name}")
    public String deleteMedicine(@PathVariable("name") String name) {
        cart.removeIf(item -> item.getName().equals(name));
        return "redirect:/";
    }

    @GetMapping("/bill")
    public String generateBill(Model model) {
        model.addAttribute("cart", cart);
        model.addAttribute("date", LocalDate.now());
        model.addAttribute("totalQty", cart.stream().mapToInt(CartItem::getQuantity).sum());
        double subtotal = cart.stream().mapToDouble(CartItem::getTotal).sum();
        model.addAttribute("subtotal", subtotal);
        return "bill"; // new bill.html page
    }
}
