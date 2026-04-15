import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router";
import { AxiosError } from "axios";
import { EyeCloseIcon, EyeIcon } from "../../icons";
import Label from "../form/Label";
import Input from "../form/input/InputField";
import Button from "../ui/button/Button";
import Alert from "../ui/alert/Alert";
import { useAuth } from "../../context/AuthContext";
import { classifyError } from "../../services/api";

export default function SignInForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [totpCode, setTotpCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<{
    title: string;
    message: string;
  } | null>(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    // Client-side validation: reject empty or whitespace-only username/password
    if (!username.trim() || !password.trim()) {
      setError({
        title: "Validation Error",
        message: "Username and password are required.",
      });
      return;
    }

    setIsLoading(true);

    try {
      const parsedTotp =
        totpCode.trim() !== "" ? Number(totpCode.trim()) : undefined;
      await login(username, password, parsedTotp);
      navigate("/");
    } catch (err) {
      if (err instanceof AxiosError) {
        const category = classifyError(err);
        switch (category) {
          case "AUTH_INVALID":
            setError({
              title: "Authentication Failed",
              message: "Invalid username or password",
            });
            break;
          case "RATE_LIMITED":
            setError({
              title: "Too Many Requests",
              message:
                "Too many login attempts. Please wait and try again.",
            });
            break;
          case "SERVER_ERROR":
          case "SERVICE_UNAVAILABLE":
            setError({
              title: "Server Error",
              message:
                "Unable to connect to the server. Please try again later.",
            });
            break;
          default:
            setError({
              title: "Error",
              message: "An unexpected error occurred. Please try again.",
            });
        }
      } else {
        setError({
          title: "Error",
          message: "An unexpected error occurred. Please try again.",
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col flex-1">
      <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
        <div>
          <div className="mb-5 sm:mb-8">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              Sign In
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Enter your credentials to access GLPI
            </p>
          </div>
          <div>
            {error && (
              <div className="mb-4">
                <Alert
                  variant="error"
                  title={error.title}
                  message={error.message}
                />
              </div>
            )}
            <form onSubmit={handleSubmit}>
              <div className="space-y-6">
                <div>
                  <Label htmlFor="username">
                    Username <span className="text-error-500">*</span>
                  </Label>
                  <Input
                    type="text"
                    id="username"
                    name="username"
                    placeholder="Enter your username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    disabled={isLoading}
                  />
                </div>
                <div>
                  <Label htmlFor="password">
                    Password <span className="text-error-500">*</span>
                  </Label>
                  <div className="relative">
                    <Input
                      type={showPassword ? "text" : "password"}
                      id="password"
                      name="password"
                      placeholder="Enter your password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      disabled={isLoading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute z-30 -translate-y-1/2 cursor-pointer right-4 top-1/2"
                      aria-label={showPassword ? "Hide password" : "Show password"}
                    >
                      {showPassword ? (
                        <EyeIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
                      ) : (
                        <EyeCloseIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
                      )}
                    </button>
                  </div>
                </div>
                <div>
                  <Label htmlFor="totpCode">
                    TOTP Code{" "}
                    <span className="text-gray-400 dark:text-gray-500">
                      (optional)
                    </span>
                  </Label>
                  <Input
                    type="text"
                    id="totpCode"
                    name="totpCode"
                    placeholder="6-digit code"
                    value={totpCode}
                    onChange={(e) => {
                      // Allow only digits, max 6 characters
                      const val = e.target.value.replace(/\D/g, "").slice(0, 6);
                      setTotpCode(val);
                    }}
                    disabled={isLoading}
                  />
                </div>
                <div>
                  <Button
                    className="w-full"
                    size="sm"
                    disabled={isLoading}
                  >
                    {isLoading ? "Signing in..." : "Sign in"}
                  </Button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
