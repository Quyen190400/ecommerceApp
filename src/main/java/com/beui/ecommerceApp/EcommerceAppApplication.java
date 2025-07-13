package com.beui.ecommerceApp;

import com.beui.ecommerceApp.repository.ProductRepository;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.Category;
import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.repository.AppUserRepository;
import com.beui.ecommerceApp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class EcommerceAppApplication {

	@Autowired
	private AppUserRepository userRepository;
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(EcommerceAppApplication.class, args);
	}

	@Bean
	public CommandLineRunner initializeData() {
		return args -> {
			// Khởi tạo dữ liệu người dùng mẫu
			if (userRepository.count() == 0) {
				
				AppUser nthquyenUser = new AppUser();
				nthquyenUser.setFullName("Nguyen Thi Hoang Quyen");
				nthquyenUser.setEmail("nthquyen@greentea.com");
				nthquyenUser.setPassword(passwordEncoder.encode("123456"));
				nthquyenUser.setUsername("nthquyen@greentea.com");
				nthquyenUser.setRole("CUSTOMER");
				userRepository.save(nthquyenUser);
				
				AppUser adminUser = new AppUser();
				adminUser.setFullName("Administrator");
				adminUser.setEmail("admin@greentea.com");
				adminUser.setPassword(passwordEncoder.encode("123456"));
				adminUser.setUsername("admin@greentea.com");
				adminUser.setRole("ADMIN");
				userRepository.save(adminUser);
			}
			System.out.println("==============================================");
			System.out.println("http://localhost:8080");
			System.out.println("Admin account created:");
			System.out.println("  Email: admin@greentea.com");
			System.out.println("  Password: 123456");
			System.out.println("==============================================");

			// Khởi tạo dữ liệu sản phẩm mẫu nếu chưa có
			if (productRepository.count() == 0) {
				System.out.println("Initializing sample products...");
				
				Product product1 = new Product();
				product1.setName("Trà xanh Thái Nguyên đặc biệt");
				product1.setDescription("Trà xanh Thái Nguyên được hái từ những lá trà non nhất, chế biến theo phương pháp truyền thống.");
				product1.setPrice(new BigDecimal("450000"));
				product1.setImageUrl("tra-thai-nguyen.jpg");
				product1.setOrigin("Thái Nguyên, Việt Nam");
				product1.setTeaType("GREEN_TEA");
				product1.setTasteNote("Đậm đà, hậu ngọt sâu lắng, hương thơm tự nhiên");
				product1.setHealthBenefit("Giúp giảm stress và lo âu, Tăng cường hệ miễn dịch, Chống oxy hóa");
				product1.setUsageGuide("1. Rửa ấm chén bằng nước nóng\n2. Cho 3-5g trà vào ấm\n3. Rót nước sôi 80-85°C\n4. Ủ 2-3 phút\n5. Thưởng thức");
				product1.setStockQuantity(50);
				product1.setSoldCount(150);
				productRepository.save(product1);
				
				Product product2 = new Product();
				product2.setName("Bộ ấm chén gốm Bát Tràng");
				product2.setDescription("Bộ ấm chén gốm Bát Tràng được làm thủ công bởi các nghệ nhân làng gốm truyền thống.");
				product2.setPrice(new BigDecimal("1200000"));
				product2.setImageUrl("am-chen-battrang.jpg");
				product2.setOrigin("Bát Tràng, Hà Nội");
				product2.setTeaType("HERBAL_TEA");
				product2.setTasteNote("Giữ nguyên hương vị trà, không bị biến chất");
				product2.setHealthBenefit("Giữ nhiệt tốt, Tôn vị trà, An toàn cho sức khỏe");
				product2.setUsageGuide("1. Rửa ấm chén bằng nước nóng\n2. Cho trà vào ấm\n3. Rót nước sôi\n4. Ủ 3-5 phút\n5. Thưởng thức");
				product2.setStockQuantity(20);
				product2.setSoldCount(30);
				productRepository.save(product2);
				
				Product product3 = new Product();
				product3.setName("Trà sen Tây Hồ cao cấp");
				product3.setDescription("Trà sen Tây Hồ được ướp hương sen tự nhiên, mang lại hương vị thanh tao, quý phái.");
				product3.setPrice(new BigDecimal("950000"));
				product3.setImageUrl("tra-sen-tay-ho.jpg");
				product3.setOrigin("Tây Hồ, Hà Nội");
				product3.setTeaType("SEN_TEA");
				product3.setTasteNote("Hương sen thơm ngát, vị trà thanh khiết");
				product3.setHealthBenefit("Thư giãn tinh thần, tốt cho tim mạch");
				product3.setUsageGuide("1. Rửa ấm chén bằng nước nóng\n2. Cho trà vào ấm\n3. Rót nước sôi 90°C\n4. Ủ 3 phút\n5. Thưởng thức");
				product3.setStockQuantity(15);
				product3.setSoldCount(67);
				productRepository.save(product3);
				
				Product product4 = new Product();
				product4.setName("Ấm tử sa nghệ nhân");
				product4.setDescription("Ấm tử sa được chế tác thủ công bởi nghệ nhân, giữ nhiệt tốt, tôn vị trà.");
				product4.setPrice(new BigDecimal("2200000"));
				product4.setImageUrl("am-tusa.jpg");
				product4.setOrigin("Nghi Hưng, Trung Quốc");
				product4.setTeaType("TEAWARE");
				product4.setTasteNote("Giữ nguyên hương vị trà, không bị biến chất");
				product4.setHealthBenefit("Giữ nhiệt tốt, tôn vị trà, an toàn cho sức khỏe");
				product4.setUsageGuide("1. Rửa ấm bằng nước nóng\n2. Cho trà vào ấm\n3. Rót nước sôi\n4. Ủ 3-5 phút\n5. Thưởng thức");
				product4.setStockQuantity(10);
				product4.setSoldCount(20);
				productRepository.save(product4);

				Product product5 = new Product();
				product5.setName("Trà hoa cúc an thần");
				product5.setDescription("Trà hoa cúc an thần giúp thư giãn, ngủ ngon, giảm stress và lo âu.");
				product5.setPrice(new BigDecimal("120000"));
				product5.setImageUrl("tra-hoa-cuc.jpg");
				product5.setOrigin("Đà Lạt, Lâm Đồng");
				product5.setTeaType("Trà hoa");
				product5.setTasteNote("Dịu nhẹ, thơm ngát với hương hoa cúc tự nhiên");
				product5.setHealthBenefit("An thần, giúp ngủ ngon, giảm stress, tốt cho mắt");
				product5.setUsageGuide("Pha với nước 85°C, ủ 3-5 phút. Uống trước khi ngủ");
				product5.setStockQuantity(40);
				product5.setSoldCount(8);
				productRepository.save(product5);

				Product product6 = new Product();
				product6.setName("Trà gừng giảm cân");
				product6.setDescription("Trà gừng giảm cân tự nhiên, hỗ trợ giảm cân, thanh lọc cơ thể, tốt cho sức khỏe.");
				product6.setPrice(new BigDecimal("95000"));
				product6.setImageUrl("tra-gung.jpg");
				product6.setOrigin("Việt Nam");
				product6.setTeaType("Trà thảo mộc");
				product6.setTasteNote("Ấm áp với vị gừng và thảo mộc tự nhiên");
				product6.setHealthBenefit("Tăng cường miễn dịch, hỗ trợ giảm cân, tốt cho tiêu hóa");
				product6.setUsageGuide("Pha với nước 90°C, ủ 5-7 phút. Uống khi ấm");
				product6.setStockQuantity(35);
				product6.setSoldCount(100);
				productRepository.save(product6);

				Product product7 = new Product();
				product7.setName("Bộ trà cụ cao cấp");
				product7.setDescription("Bộ trà cụ cao cấp gồm ấm, chén, phụ kiện trà đạo tinh xảo, thích hợp làm quà tặng.");
				product7.setPrice(new BigDecimal("1850000"));
				product7.setImageUrl("bo-tra-cu.jpg");
				product7.setOrigin("Bát Tràng, Hà Nội");
				product7.setTeaType("Dụng cụ trà");
				product7.setTasteNote("Giữ nguyên hương vị trà, không bị biến chất");
				product7.setHealthBenefit("Giữ nhiệt tốt, tôn vị trà, an toàn cho sức khỏe");
				product7.setUsageGuide("1. Rửa ấm chén bằng nước nóng\n2. Cho trà vào ấm\n3. Rót nước sôi\n4. Ủ 3-5 phút\n5. Thưởng thức");
				product7.setStockQuantity(12);
				product7.setSoldCount(8);
				productRepository.save(product7);

				Product product8 = new Product();
				product8.setName("Trà Oolong Đài Loan");
				product8.setDescription("Trà Oolong Đài Loan cao cấp với hương vị phức tạp, từ ngọt đến chát.");
				product8.setPrice(new BigDecimal("250000"));
				product8.setImageUrl("tra-oolong.jpg");
				product8.setOrigin("Đài Loan");
				product8.setTeaType("Trà Oolong");
				product8.setTasteNote("Phức tạp với nhiều tầng hương vị, từ ngọt đến chát");
				product8.setHealthBenefit("Tăng cường trao đổi chất, giảm cholesterol, tốt cho tim");
				product8.setUsageGuide("Pha với nước 85-95°C, ủ 3-5 phút. Có thể pha nhiều lần");
				product8.setStockQuantity(18);
				product8.setSoldCount(7);
				productRepository.save(product8);

				Product product9 = new Product();
				product9.setName("Bộ ấm chén gốm sứ Bát Tràng thượng hạng");
				product9.setDescription("Bộ ấm chén gốm sứ Bát Tràng thượng hạng được làm thủ công, gồm ấm, 6 chén, khay trà và phụ kiện đầy đủ.");
				product9.setPrice(new BigDecimal("2800000"));
				product9.setImageUrl("bo-am-tra.jpg");
				product9.setOrigin("Bát Tràng, Hà Nội");
				product9.setTeaType("Dụng cụ trà");
				product9.setTasteNote("Giữ nguyên hương vị trà, không bị biến chất");
				product9.setHealthBenefit("Giữ nhiệt tốt, tôn vị trà, an toàn cho sức khỏe");
				product9.setUsageGuide("1. Rửa ấm chén bằng nước nóng\n2. Cho trà vào ấm\n3. Rót nước sôi\n4. Ủ 3-5 phút\n5. Thưởng thức");
				product9.setStockQuantity(8);
				product9.setSoldCount(15);
				productRepository.save(product9);

				Product product10 = new Product();
				product10.setName("Bộ ấm tử sa Nghi Hưng cao cấp");
				product10.setDescription("Bộ ấm tử sa Nghi Hưng cao cấp được chế tác bởi nghệ nhân bậc thầy, gồm ấm tử sa, 4 chén, khay gỗ và dụng cụ trà đạo.");
				product10.setPrice(new BigDecimal("3500000"));
				product10.setImageUrl("bo-am-tu-sa.jpg");
				product10.setOrigin("Nghi Hưng, Trung Quốc");
				product10.setTeaType("Dụng cụ trà");
				product10.setTasteNote("Giữ nguyên hương vị trà, không bị biến chất");
				product10.setHealthBenefit("Giữ nhiệt tốt, tôn vị trà, an toàn cho sức khỏe");
				product10.setUsageGuide("1. Rửa ấm bằng nước nóng\n2. Cho trà vào ấm\n3. Rót nước sôi\n4. Ủ 3-5 phút\n5. Thưởng thức");
				product10.setStockQuantity(5);
				product10.setSoldCount(12);
				productRepository.save(product10);
				
				System.out.println("✅ Sample products initialized successfully!");
			}

			if (categoryRepository.count() == 0) {
				List<Category> categories = Arrays.asList(
						new Category("Bộ ấm trà", "Các loại bộ ấm chén trà cao cấp, dụng cụ trà đạo truyền thống"),
						new Category("Trà Thảo Mộc", "Trà từ các loại thảo mộc tự nhiên, tốt cho sức khỏe"),
						new Category("Trà Hoa", "Trà được ướp với các loại hoa tự nhiên"),
						new Category("Trà Xanh", "Trà xanh tươi với nhiều lợi ích sức khỏe")
				);

				categoryRepository.saveAll(categories);
				System.out.println("✅ Categories initialized successfully! Count: " + categories.size());

			}
		};
	}
}
