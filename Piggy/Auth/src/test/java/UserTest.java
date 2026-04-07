import auth.AuthApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证服务接口测试类
 *
 * @author: rj
 */
@SpringBootTest(classes = AuthApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    @Autowired
    private WebApplicationContext webApplicationContext; // SpringBoot测试类中，需要注入WebApplicationContext对象

    private MockMvc mockMvc; // MockMvc对象用于模拟HTTP请求

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 测试1：用户注册 - 正常场景
     * 验证：使用合法参数注册新用户，应该返回成功
     */
    @Test
    @Order(1)
    @DisplayName("注册-成功-使用合法参数注册新用户")
    void test_Register_Success_WithValidParams() throws Exception {
        String requestBody = """
                {
                    "username": "testuser01",
                    "password": "test123456",
                    "nickname": "测试用户"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.username").value("testuser01"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    /**
     * 测试2：用户注册 - 用户名已存在
     * 验证：使用已存在的用户名注册，应该返回业务异常（HTTP 400）
     */
    @Test
    @Order(2)
    @DisplayName("注册-失败-用户名已存在")
    void test_Register_Fail_UsernameAlreadyExists() throws Exception {
        String requestBody = """
                {
                    "username": "testuser01",
                    "password": "test123456",
                    "nickname": "测试用户"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    /**
     * 测试3：用户注册 - 用户名为空
     * 验证：用户名为空时，应该返回参数校验失败（HTTP 400）
     */
    @Test
    @Order(3)
    @DisplayName("注册-失败-用户名为空")
    void test_Register_Fail_UsernameBlank() throws Exception {
        String requestBody = """
                {
                    "username": "",
                    "password": "test123456",
                    "nickname": "测试用户"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试4：用户注册 - 密码格式不正确
     * 验证：密码不包含字母和数字时，应该返回参数校验失败（HTTP 400）
     */
    @Test
    @Order(4)
    @DisplayName("注册-失败-密码格式不正确")
    void test_Register_Fail_InvalidPasswordFormat() throws Exception {
        String requestBody = """
                {
                    "username": "testuser02",
                    "password": "123456789",
                    "nickname": "测试用户"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试5：用户登录 - 正常场景
     * 验证：使用正确的用户名和密码登录，应该返回成功
     */
    @Test
    @Order(5)
    @DisplayName("登录-成功-使用正确的用户名和密码")
    void test_Login_Success_WithValidCredentials() throws Exception {
        String requestBody = """
                {
                    "username": "testuser01",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.username").value("testuser01"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    /**
     * 测试6：用户登录 - 用户不存在
     * 验证：使用不存在的用户名登录，应该返回业务异常（HTTP 400）
     */
    @Test
    @Order(6)
    @DisplayName("登录-失败-用户不存在")
    void test_Login_Fail_UserNotFound() throws Exception {
        String requestBody = """
                {
                    "username": "nonexistentuser",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    /**
     * 测试7：用户登录 - 密码错误
     * 验证：使用错误的密码登录，应该返回业务异常（HTTP 400）
     */
    @Test
    @Order(7)
    @DisplayName("登录-失败-密码错误")
    void test_Login_Fail_WrongPassword() throws Exception {
        String requestBody = """
                {
                    "username": "testuser01",
                    "password": "wrongpassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    /**
     * 测试8：用户登录 - 用户名为空
     * 验证：用户名为空时，应该返回参数校验失败（HTTP 400）
     */
    @Test
    @Order(8)
    @DisplayName("登录-失败-用户名为空")
    void test_Login_Fail_UsernameBlank() throws Exception {
        String requestBody = """
                {
                    "username": "",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试9：刷新Token - 正常场景
     * 验证：使用有效的refreshToken刷新，应该返回新的token
     */
    @Test
    @Order(9)
    @DisplayName("刷新Token-成功-使用有效的refreshToken")
    void test_RefreshToken_Success_WithValidToken() throws Exception {
        String loginRequest = """
                {
                    "username": "testuser01",
                    "password": "test123456"
                }
                """;

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int refreshTokenStart = loginResponse.indexOf("\"refreshToken\":\"") + 16;
        int refreshTokenEnd = loginResponse.indexOf("\"", refreshTokenStart);
        String refreshToken = loginResponse.substring(refreshTokenStart, refreshTokenEnd);

        String refreshRequest = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Token 刷新成功"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    /**
     * 测试10：刷新Token - Token为空
     * 验证：refreshToken为空时，应该返回参数校验失败（HTTP 400）
     */
    @Test
    @Order(10)
    @DisplayName("刷新Token-失败-Token为空")
    void test_RefreshToken_Fail_TokenBlank() throws Exception {
        String requestBody = """
                {
                    "refreshToken": ""
                }
                """;

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试11：刷新Token - Token无效
     * 验证：使用无效的refreshToken，应该返回业务异常（HTTP 400）
     */
    @Test
    @Order(11)
    @DisplayName("刷新Token-失败-Token无效")
    void test_RefreshToken_Fail_InvalidToken() throws Exception {
        String requestBody = """
                {
                    "refreshToken": "invalid.token.here"
                }
                """;

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Refresh Token 无效"));
    }
}
