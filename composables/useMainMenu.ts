import { computed } from "vue";
import { useProcessSubmenu } from "~/composables/useProcessMenu";

export type MenuItem = {
  key: string;
  label: string;
  icon?: string;
  to?: string;
  href?: string;
  children?: MenuItem[];
};

export function useMainMenu() {
  const { processes, getLastKey } = useProcessSubmenu();

  const menu = computed<MenuItem[]>(() => [
    {
      key: "home",
      label: "Dashboard",
      icon: "fa-solid fa-tachometer-alt",
      to: "/",
    },
    { key: "search", label: "Pesquisar", icon: "fa-solid fa-magnifying-glass" },
    { key: "ai", label: "Assistente", icon: "fa-solid fa-brain ai-brain" },
    {
      key: "processos",
      label: "Processos",
      icon: "fa-solid fa-chart-simple",
      to: (() => {
        const last = getLastKey();
        return last ? `/esteira/${last}` : "/processos";
      })(),
      children: [
        // Section headers recognized by SidebarSubmenu
        /*  { key: 'sec-main', label: 'Main Menu', type: 'section' } as any, */
        {
          key: "ver-processos",
          label: "Ver Processos",
          icon: "fa-solid fa-table-cells-large",
          to: "/processos",
        },
        ...processes.value.map((p) => ({
          key: p.key,
          label: p.name,
          icon: "fa-regular fa-file-lines",
          to: `/esteira/${p.key}`,
        })),
        { key: "sec-pref", label: "Preferences", type: "section" } as any,
        {
          key: "settings",
          label: "Settings",
          icon: "fa-solid fa-gear",
          to: "/admin?tab=pipeline",
        },
      ],
    },
    {
      key: "admin",
      label: "Admin",
      icon: "fa-solid fa-shield-halved",
      to: "/admin",
      children: [
        { key: "sec-admin-main", label: "Main Menu", type: "section" } as any,
        {
          key: "admin-users",
          label: "Usuários",
          icon: "fa-regular fa-user",
          href: "/admin?tab=users",
        },
        {
          key: "admin-pipeline",
          label: "Gestão de Esteira",
          icon: "fa-solid fa-briefcase",
          href: "/admin?tab=pipeline",
        },
        { key: "sec-admin-pref", label: "Preferences", type: "section" } as any,
        {
          key: "admin-notifications",
          label: "Notificações",
          icon: "fa-regular fa-bell",
          href: "/admin?tab=notifications",
        },
        {
          key: "help",
          label: "Ajuda",
          icon: "fa-regular fa-circle-question",
          href: "/help",
        },
      ],
    },
  ]);

  return { menu };
}
