package com.study.springflow.config;

import com.study.springflow.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * ✅ Spring MVC 핵심 설정 클래스
     * - WebMvcConfigurer 인터페이스 구현으로 다양한 MVC 설정 커스터마이징
     * - DispatcherServlet의 구성 요소들을 세부 조정
     * - 이 클래스는 Spring Boot의 자동 구성을 확장하는 역할
     */

    /**
     * 정적 리소스 핸들러 설정
     * - 정적 리소스(CSS, JS, 이미지)에 대한 요청 처리 설정
     * - 웹 브라우저의 /static/** 요청을 classpath:/static/ 경로의 파일로 매핑
     *
     * 🔍 추가 활용 옵션:
     * 1. 다중 리소스 위치 설정:
     *    registry.addResourceHandler("/resources/**")
     *            .addResourceLocations("classpath:/static/", "classpath:/public/", "file:/opt/files/");
     *
     * 2. 리소스 캐싱 설정:
     *    registry.addResourceHandler("/static/**")
     *            .addResourceLocations("classpath:/static/")
     *            .setCachePeriod(3600) // 초 단위 캐시 기간
     *            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
     *
     * 3. 리소스 체인 및 버전 관리:
     *    ResourceChainRegistration chain = registry.addResourceHandler("/resources/**")
     *            .addResourceLocations("classpath:/static/")
     *            .resourceChain(true);
     *
     *    chain.addResolver(new VersionResourceResolver()
     *            .addContentVersionStrategy("/**")); // 컨텐츠 해시 기반 버전 관리
     *
     * 4. 웹JAR 리소스 설정:
     *    registry.addResourceHandler("/webjars/**")
     *            .addResourceLocations("classpath:/META-INF/resources/webjars/");
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     * - 다른 도메인에서의 API 호출 허용 설정
     * - /api/** 경로에 대한 크로스 도메인 요청 처리 규칙 정의
     *
     * ⚠️ 주의사항:
     * - allowedOrigins("*")와 allowCredentials(true)를 함께 사용하면 CORS 명세 위반으로 오류 발생
     * - Spring 5.3부터 allowedOrigins("*") 대신 allowedOriginPatterns("*") 사용 권장
     *
     * 🔍 추가 활용 옵션:
     * 1. 특정 도메인만 허용:
     *    registry.addMapping("/api/**")
     *            .allowedOrigins("https://trusted-client.com", "https://admin.company.com")
     *            .allowedMethods("GET", "POST");
     *
     * 2. 모든 경로 CORS 설정:
     *    registry.addMapping("/**")
     *            .allowedOriginPatterns("*") // Spring 5.3+ 권장 방식
     *            .allowedMethods("*")
     *            .maxAge(3600L);
     *
     * 3. 인증 포함 요청 설정:
     *    registry.addMapping("/secure/**")
     *            .allowedOrigins("https://trusted-site.com")
     *            .allowedMethods("GET", "POST")
     *            .allowCredentials(true)
     *            .exposedHeaders("Authorization");
     *
     * 4. 특정 헤더만 허용:
     *    registry.addMapping("/api/v2/**")
     *            .allowedOriginPatterns("*")
     *            .allowedMethods("GET", "POST", "PUT")
     *            .allowedHeaders("Content-Type", "X-Requested-With", "Authorization");
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // allowedOrigins("*")와 allowCredentials(true)는 함께 사용할 수 없음
                // Spring 5.3부터는 allowedOriginPatterns("*") 사용
                .allowedOriginPatterns("*") // 모든 출처를 허용하면서 credentials도 허용
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name()
                )
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 프리플라이트 요청 캐시 시간(초)
    }

    /**
     * 인터셉터 등록
     * - 컨트롤러 실행 전/후 처리 로직 설정
     * - AuthInterceptor를 모든 경로(/**)에 적용하되 정적 리소스와 에러 페이지는 제외
     *
     * 🔍 추가 활용 옵션:
     * 1. 다중 인터셉터 체인:
     *    // 1. 로깅 인터셉터 (모든 요청)
     *    registry.addInterceptor(new LoggingInterceptor())
     *            .addPathPatterns("/**")
     *            .order(0);
     *
     *    // 2. 인증 인터셉터 (보안 영역)
     *    registry.addInterceptor(new AuthInterceptor())
     *            .addPathPatterns("/admin/**", "/secure/**")
     *            .excludePathPatterns("/admin/login")
     *            .order(1);
     *
     *    // 3. 성능 측정 인터셉터 (API 요청만)
     *    registry.addInterceptor(new PerformanceInterceptor())
     *            .addPathPatterns("/api/**")
     *            .order(2);
     *
     * 2. 경로 패턴 세분화:
     *    registry.addInterceptor(new AdminInterceptor())
     *            .addPathPatterns("/admin/**")
     *            .excludePathPatterns(
     *                "/admin/login",
     *                "/admin/assets/**",
     *                "/admin/public/**"
     *            );
     *
     * 3. 어드민 권한 검사 인터셉터:
     *    registry.addInterceptor(new AdminAuthInterceptor())
     *            .addPathPatterns("/admin/**")
     *            .excludePathPatterns("/admin/login", "/admin/logout");
     *
     * 4. API 요청 제한 인터셉터:
     *    registry.addInterceptor(new RateLimitInterceptor())
     *            .addPathPatterns("/api/**");
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error")
                .order(1);
    }

    /**
     * 뷰 컨트롤러 등록
     * - 단순 뷰 이동만 하는 컨트롤러 대체 설정
     * - /login 요청을 login 뷰로 바로 연결 (Controller 없이)
     *
     * 🔍 추가 활용 옵션:
     * 1. 홈페이지 및 기본 페이지 설정:
     *    registry.addViewController("/").setViewName("home");
     *    registry.addViewController("/index").setViewName("home");
     *
     * 2. 리디렉션 컨트롤러:
     *    registry.addRedirectViewController("/old-path", "/new-path");
     *
     * 3. 상태 코드 컨트롤러:
     *    registry.addStatusController("/health", HttpStatus.OK);
     *
     * 4. 단순 정적 페이지 매핑:
     *    registry.addViewController("/about").setViewName("about");
     *    registry.addViewController("/contact").setViewName("contact");
     *    registry.addViewController("/terms").setViewName("terms");
     *    registry.addViewController("/privacy").setViewName("privacy");
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.setOrder(0);
    }

    /**
     * 경로 매치 설정
     * - URL 요청 경로 처리 방식 설정
     * - 패턴 매칭 파서 설정 (Spring 5.3+에서 변경됨)
     *
     * ⚠️ 주의사항:
     * - Spring 5.3부터 setUseTrailingSlashMatch(), setUseSuffixPatternMatch() 메서드 deprecated
     * - 대신 PathPatternParser 사용 (AntPathMatcher 대체)
     * - PathPatternParser는 더 효율적이고 성능이 좋은 URL 패턴 매칭 제공
     *
     * 🔍 현대적인 경로 매칭 설정 방법:
     * 1. PathPatternParser 적용:
     *    - 자동으로 후행 슬래시('/') 매칭 비활성화
     *    - 확장자 패턴 매칭 비활성화
     *    - 더 엄격한 경로 매칭 (보안 향상)
     *    - 더 빠른 경로 매칭 성능
     *
     * 2. 매트릭스 변수 활성화:
     *    // Spring 5.3+에서 더 이상 setRemoveSemicolonContent()를 사용하지 않음
     *    // PathPattern 자체에서 매트릭스 변수를 지원함
     *    // (예시 URL: /users/42;role=admin;status=active)
     *
     * 3. 경로 패턴 매칭 추가 옵션:
     *    configurer.addPathPrefix("/api",
     *            HandlerTypePredicate.forAnnotation(RestController.class));
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 최신 방식으로 경로 매칭 설정 (Spring 5.3+)
        // deprecated된 setUseTrailingSlashMatch(), setUseSuffixPatternMatch() 대신 PathPatternParser 사용
        PathPatternParser pathPatternParser = new PathPatternParser();
        configurer.setPatternParser(pathPatternParser);

        // 특정 패키지/어노테이션 기반 경로 프리픽스 추가 예시:
        // REST API 컨트롤러 자동 매핑 (선택 사항)
        // configurer.addPathPrefix("/api",
        //     HandlerTypePredicate.forAnnotation(RestController.class));
    }

    /**
     * 컨트롤러 메서드 인자 리졸버 등록
     * - 컨트롤러 메서드 파라미터 처리 방식 설정
     * - 현재는 주석처리된 CustomUserArgumentResolver 예시
     *
     * 🔍 추가 활용 옵션:
     * 1. 현재 사용자 주입 리졸버:
     *    @Component
     *    public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
     *        @Override
     *        public boolean supportsParameter(MethodParameter parameter) {
     *            return parameter.hasParameterAnnotation(CurrentUser.class)
     *                   && parameter.getParameterType().equals(User.class);
     *        }
     *
     *        @Override
     *        public Object resolveArgument(MethodParameter parameter,
     *                                      ModelAndViewContainer mavContainer,
     *                                      NativeWebRequest webRequest,
     *                                      WebDataBinderFactory binderFactory) {
     *            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
     *            HttpSession session = request.getSession(false);
     *            if (session != null) {
     *                return session.getAttribute("currentUser");
     *            }
     *            return null;
     *        }
     *    }
     *
     * 2. 페이지네이션 파라미터 리졸버:
     *    public class PageableArgumentResolver implements HandlerMethodArgumentResolver {
     *        @Override
     *        public boolean supportsParameter(MethodParameter parameter) {
     *            return parameter.getParameterType().equals(Pageable.class);
     *        }
     *
     *        @Override
     *        public Object resolveArgument(MethodParameter parameter,
     *                                     ModelAndViewContainer mavContainer,
     *                                     NativeWebRequest webRequest,
     *                                     WebDataBinderFactory binderFactory) {
     *            String pageStr = webRequest.getParameter("page");
     *            String sizeStr = webRequest.getParameter("size");
     *
     *            int page = StringUtils.isEmpty(pageStr) ? 0 : Integer.parseInt(pageStr);
     *            int size = StringUtils.isEmpty(sizeStr) ? 20 : Integer.parseInt(sizeStr);
     *
     *            return PageRequest.of(page, size);
     *        }
     *    }
     *
     * 3. JSON 요청 본문 변환 리졸버:
     *    public class JsonBodyArgumentResolver implements HandlerMethodArgumentResolver {
     *        private final ObjectMapper objectMapper = new ObjectMapper();
     *
     *        @Override
     *        public boolean supportsParameter(MethodParameter parameter) {
     *            return parameter.hasParameterAnnotation(JsonBody.class);
     *        }
     *
     *        @Override
     *        public Object resolveArgument(MethodParameter parameter,
     *                                     ModelAndViewContainer mavContainer,
     *                                     NativeWebRequest webRequest,
     *                                     WebDataBinderFactory binderFactory) throws Exception {
     *            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
     *            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
     *            return objectMapper.readValue(body, parameter.getParameterType());
     *        }
     *    }
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // resolvers.add(new CustomUserArgumentResolver());
    }

    /**
     * 예외 처리기 등록
     * - 컨트롤러에서 발생한 예외 처리 방법 설정
     * - 현재는 주석처리된 CustomExceptionResolver 예시
     *
     * 🔍 추가 활용 옵션:
     * 1. 상태 코드별 예외 처리:
     *    @Bean
     *    public SimpleMappingExceptionResolver exceptionResolver() {
     *        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
     *
     *        Properties mappings = new Properties();
     *        mappings.setProperty(EntityNotFoundException.class.getName(), "error/not-found");
     *        mappings.setProperty(AccessDeniedException.class.getName(), "error/forbidden");
     *        mappings.setProperty(Exception.class.getName(), "error/server-error");
     *
     *        resolver.setExceptionMappings(mappings);
     *        resolver.setDefaultErrorView("error/default");
     *        resolver.setExceptionAttribute("exception");
     *        resolver.setWarnLogCategory("com.study.springflow.exception");
     *
     *        return resolver;
     *    }
     *
     * 2. REST API용 예외 처리:
     *    public class RestExceptionResolver extends AbstractHandlerExceptionResolver {
     *        private final ObjectMapper objectMapper = new ObjectMapper();
     *
     *        @Override
     *        protected ModelAndView doResolveException(HttpServletRequest request,
     *                                                 HttpServletResponse response,
     *                                                 Object handler,
     *                                                 Exception ex) {
     *            try {
     *                if (ex instanceof EntityNotFoundException) {
     *                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
     *                } else if (ex instanceof IllegalArgumentException) {
     *                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     *                } else {
     *                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     *                }
     *
     *                response.setContentType("application/json");
     *                response.setCharacterEncoding("UTF-8");
     *
     *                Map<String, Object> errorData = new HashMap<>();
     *                errorData.put("message", ex.getMessage());
     *                errorData.put("status", response.getStatus());
     *                errorData.put("timestamp", LocalDateTime.now());
     *
     *                response.getWriter().write(objectMapper.writeValueAsString(errorData));
     *
     *                return new ModelAndView(); // 뷰 렌더링하지 않음
     *            } catch (Exception e) {
     *                return null; // 처리 실패 시 다음 리졸버로 넘김
     *            }
     *        }
     *    }
     *
     * 3. 디버깅용 상세 예외 처리:
     *    public class DetailedExceptionResolver implements HandlerExceptionResolver {
     *        @Override
     *        public ModelAndView resolveException(HttpServletRequest request,
     *                                            HttpServletResponse response,
     *                                            Object handler,
     *                                            Exception ex) {
     *            ModelAndView mav = new ModelAndView("error/detailed");
     *            mav.addObject("exception", ex);
     *            mav.addObject("handler", handler);
     *            mav.addObject("url", request.getRequestURL());
     *            mav.addObject("timestamp", LocalDateTime.now());
     *            mav.addObject("trace", ExceptionUtils.getStackTrace(ex));
     *
     *            return mav;
     *        }
     *    }
     */
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        // resolvers.add(new CustomExceptionResolver());
    }

    /**
     * 추가 가능한 다른 메서드들:
     *
     * 1. 메시지 컨버터 구성:
     *    @Override
     *    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
     *        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8)); // Charset.forName() 대신 StandardCharsets 사용
     *
     *        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
     *        ObjectMapper objectMapper = jsonConverter.getObjectMapper();
     *        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
     *        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
     *
     *        converters.add(jsonConverter);
     *        converters.add(new MappingJackson2XmlHttpMessageConverter());
     *    }
     *
     * 2. 비동기 처리 설정:
     *    @Override
     *    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
     *        configurer.setDefaultTimeout(30_000); // 30초
     *        configurer.setTaskExecutor(taskExecutor());
     *        configurer.registerCallableInterceptors(timeoutInterceptor());
     *        configurer.registerDeferredResultInterceptors(loggingInterceptor());
     *    }
     *
     *    @Bean
     *    public ThreadPoolTaskExecutor taskExecutor() {
     *        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
     *        executor.setCorePoolSize(10);
     *        executor.setMaxPoolSize(100);
     *        executor.setQueueCapacity(50);
     *        executor.setThreadNamePrefix("async-");
     *        return executor;
     *    }
     *
     * 3. 컨텐츠 협상 설정:
     *    @Override
     *    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
     *        configurer
     *            .favorParameter(true) // URL 파라미터 사용: ?format=json
     *            .parameterName("format")
     *            .ignoreAcceptHeader(false) // Accept 헤더 사용
     *            .defaultContentType(MediaType.APPLICATION_JSON)
     *            .mediaType("json", MediaType.APPLICATION_JSON)
     *            .mediaType("xml", MediaType.APPLICATION_XML)
     *            .mediaType("html", MediaType.TEXT_HTML);
     *    }
     *
     * 4. 포매터 및 컨버터 등록:
     *    @Override
     *    public void addFormatters(FormatterRegistry registry) {
     *        // 문자열 → Enum 변환기
     *        registry.addConverter(String.class, RoleType.class,
     *                              source -> RoleType.valueOf(source.toUpperCase()));
     *
     *        // 날짜 포맷터
     *        registry.addFormatter(new DateFormatter("yyyy-MM-dd"));
     *    }
     */
}