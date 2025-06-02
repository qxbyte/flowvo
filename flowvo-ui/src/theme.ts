import { extendTheme, type ThemeConfig } from "@chakra-ui/react";

// 设置颜色模式配置
const config: ThemeConfig = {
  initialColorMode: "system",
  useSystemColorMode: true,
};

// 自定义主题
const theme = extendTheme({
  config,
  colors: {
    // 使用黑白灰色调
    gray: {
      50: "#F7FAFC",
      100: "#EDF2F7",
      200: "#E2E8F0",
      300: "#CBD5E0",
      400: "#A0AEC0",
      500: "#718096",
      600: "#4A5568",
      700: "#2D3748",
      800: "#1A202C",
      900: "#171923",
    },
    // 为保持一致性，蓝色保留但调整为更灰暗的色调
    blue: {
      50: "#E6EEF8",
      100: "#C9DCF0",
      200: "#9CB9DE",
      300: "#6E95CD",
      400: "#4172BC",
      500: "#3161A9", // 主色调，更灰暗
      600: "#284D87",
      700: "#1E3A65",
      800: "#152744",
      900: "#0B1322",
    },
  },
  fonts: {
    heading: "system-ui, sans-serif",
    body: "system-ui, sans-serif",
  },
  components: {
    Button: {
      baseStyle: {
        borderRadius: "12px", // 更圆润的按钮
        fontWeight: "500",
      },
      variants: {
        solid: {
          bg: "gray.700",
          color: "white",
          _hover: {
            bg: "gray.800",
          },
        },
        outline: {
          borderColor: "gray.300",
          color: "gray.700",
          _hover: {
            bg: "gray.50",
          },
        },
        ghost: {
          color: "gray.600",
          _hover: {
            bg: "gray.100",
          },
        },
      },
    },
    Input: {
      baseStyle: {
        field: {
          borderRadius: "12px",
        },
      },
      variants: {
        outline: {
          field: {
            borderColor: "gray.300",
          },
        },
      },
    },
    Card: {
      baseStyle: {
        container: {
          borderRadius: "16px",
          boxShadow: "sm",
        },
      },
    },
    Badge: {
      baseStyle: {
        borderRadius: "8px",
      },
    },
    Textarea: {
      baseStyle: {
        borderRadius: "16px",
      },
    },
    Tag: {
      baseStyle: {
        container: {
          borderRadius: "8px",
        },
      },
    },
    Container: {
      baseStyle: {
        maxW: "100%",
        px: 4,
      },
    },
  },
  styles: {
    global: (props: any) => ({
      html: {
        height: "100%",
        width: "100%"
      },
      body: {
        bg: props.colorMode === "dark" ? "#1B212C" : "gray.50",
        color: props.colorMode === "dark" ? "gray.100" : "gray.800",
        minHeight: "100%",
        width: "100%",
        margin: 0,
        padding: 0,
      },
      "#root": {
        width: "100%",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
      }
    }),
  },
});

export default theme; 