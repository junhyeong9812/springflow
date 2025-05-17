package com.study.springflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class ViewResolverConfig {

    /**
     * ✅ JSP 기반 ViewResolver 설정
     * - Spring Boot는 자동 설정되지만, 여기서는 명시적으로 수동 등록
     * - 예: "hello" → /WEB-INF/views/hello.jsp
     *
     * 🔧 추가 설정:
     * - resolver.setExposeContextBeansAsAttributes(true); → 컨텍스트 내 모든 Bean 모델 노출
     * - resolver.setViewNames("*.jsp"); → 특정 뷰 이름만 매핑
     *
     * 🔍 추가 활용 옵션:
     * 1. 다중 ViewResolver 구성:
     *    @Bean
     *    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager,
     *                                                      List<ViewResolver> resolvers) {
     *        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
     *        resolver.setContentNegotiationManager(manager);
     *        resolver.setViewResolvers(resolvers);
     *        resolver.setOrder(-1); // 최우선 순위
     *
     *        // 기본 뷰 등록
     *        Map<String, MediaType> mediaTypes = new HashMap<>();
     *        mediaTypes.put("html", MediaType.TEXT_HTML);
     *        mediaTypes.put("json", MediaType.APPLICATION_JSON);
     *        mediaTypes.put("xml", MediaType.APPLICATION_XML);
     *        resolver.setMediaTypes(mediaTypes);
     *
     *        return resolver;
     *    }
     *
     * 2. Thymeleaf ViewResolver 등록:
     *    @Bean
     *    public SpringResourceTemplateResolver templateResolver() {
     *        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
     *        resolver.setPrefix("classpath:/templates/");
     *        resolver.setSuffix(".html");
     *        resolver.setTemplateMode("HTML");
     *        resolver.setCharacterEncoding("UTF-8");
     *        resolver.setCacheable(false); // 개발 시에는 캐시 비활성화
     *        return resolver;
     *    }
     *
     *    @Bean
     *    public SpringTemplateEngine templateEngine() {
     *        SpringTemplateEngine engine = new SpringTemplateEngine();
     *        engine.setTemplateResolver(templateResolver());
     *        engine.setEnableSpringELCompiler(true);
     *        return engine;
     *    }
     *
     *    @Bean
     *    public ViewResolver thymeleafViewResolver() {
     *        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
     *        resolver.setTemplateEngine(templateEngine());
     *        resolver.setCharacterEncoding("UTF-8");
     *        resolver.setOrder(1); // JSP ViewResolver보다 낮은 우선순위
     *        resolver.setViewNames(new String[] {"thymeleaf/*"});
     *        return resolver;
     *    }
     *
     * 3. JSON/XML 전용 ViewResolver:
     *    @Bean
     *    public BeanNameViewResolver beanNameViewResolver() {
     *        BeanNameViewResolver resolver = new BeanNameViewResolver();
     *        resolver.setOrder(10);
     *        return resolver;
     *    }
     *
     *    @Bean(name = "jsonView")
     *    public MappingJackson2JsonView jsonView() {
     *        MappingJackson2JsonView view = new MappingJackson2JsonView();
     *        view.setPrettyPrint(true);
     *        view.setExtractValueFromSingleKeyModel(true);
     *        return view;
     *    }
     *
     *    @Bean(name = "xmlView")
     *    public MappingJackson2XmlView xmlView() {
     *        MappingJackson2XmlView view = new MappingJackson2XmlView();
     *        view.setPrettyPrint(true);
     *        return view;
     *    }
     *
     * 4. 다국어 메시지 리졸버:
     *    @Bean
     *    public MessageSource messageSource() {
     *        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
     *        messageSource.setBasenames("messages");
     *        messageSource.setDefaultEncoding("UTF-8");
     *        return messageSource;
     *    }
     *
     * 5. 에러 페이지 ViewResolver:
     *    @Bean
     *    public SimpleMappingExceptionResolver exceptionResolver() {
     *        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
     *
     *        Properties mappings = new Properties();
     *        mappings.setProperty(DataAccessException.class.getName(), "error/database");
     *        mappings.setProperty(AccessDeniedException.class.getName(), "error/forbidden");
     *        mappings.setProperty(Exception.class.getName(), "error/error");
     *
     *        resolver.setExceptionMappings(mappings);
     *        resolver.setDefaultErrorView("error/default");
     *        resolver.setExceptionAttribute("exception"); // JSP에서 ${exception} 변수로 접근
     *        resolver.setOrder(0); // 최우선 순위
     *
     *        return resolver;
     *    }
     *
     * 🔧 흐름 테스트 방법:
     * 1. 다양한 뷰 타입 처리 확인:
     *    @GetMapping("/test-view")
     *    public String testView(Model model) {
     *        model.addAttribute("message", "뷰 리졸버 테스트");
     *        return "testView"; // ViewResolver 체인에 따라 처리
     *    }
     *
     * 2. Accept 헤더 기반 뷰 선택 테스트:
     *    @GetMapping("/test-content-negotiation")
     *    public String testContentNegotiation(Model model) {
     *        model.addAttribute("data", Map.of("key", "value"));
     *        return "result"; // Accept: application/json이면 JSON으로, text/html이면 HTML로 응답
     *    }
     *
     * 3. 명시적 뷰 선택:
     *    @GetMapping("/test-explicit-view")
     *    public ModelAndView testExplicitView() {
     *        ModelAndView mav = new ModelAndView("jsonView"); // BeanNameViewResolver로 처리
     *        mav.addObject("data", Map.of("message", "명시적 뷰 선택 테스트"));
     *        return mav;
     *    }
     *
     * 4. 예외 처리 흐름 확인:
     *    @GetMapping("/test-exception")
     *    public String testException() {
     *        throw new DataAccessException("데이터베이스 예외 테스트") {};
     *        // SimpleMappingExceptionResolver가 "error/database" 뷰로 연결
     *    }
     */
    @Bean
    public ViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");    // 뷰 파일 경로
        resolver.setSuffix(".jsp");               // 확장자
        resolver.setOrder(0);                     // 우선순위 (낮을수록 우선)
        return resolver;
    }
}