#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для поиска и удаления комментариев из Java-кода.
Ловит:
  - Standalone строчные:  // текст
  - Инлайн в конце кода:  code(); // текст   (удаляет только комментарий)
  - Javadoc блоки:        /** ... */
Для каждого найденного — спрашивает Y/T/S/Q.
"""

import os
import re
import sys
import shutil
from pathlib import Path

# ─── ЦВЕТА ───────────────────────────────────────────────────────────────────
class C:
    RESET   = "\033[0m"
    BOLD    = "\033[1m"
    DIM     = "\033[2m"
    RED     = "\033[91m"
    GREEN   = "\033[92m"
    YELLOW  = "\033[93m"
    BLUE    = "\033[94m"
    MAGENTA = "\033[95m"
    CYAN    = "\033[96m"

try:
    import ctypes
    ctypes.windll.kernel32.SetConsoleMode(ctypes.windll.kernel32.GetStdHandle(-11), 7)
except Exception:
    pass

# ─── ПАТТЕРНЫ ────────────────────────────────────────────────────────────────

# Строка — целиком комментарий (пробелы + //)
RE_STANDALONE = re.compile(r'^(\s*)//(.*)$')

# Код + инлайн-комментарий в конце строки
# Группа 1 — часть с кодом, группа 2 — текст комментария
RE_INLINE = re.compile(r'^(.*\S)\s*//(.+)$')

# Начало javadoc-блока /** или /*
RE_BLOCK_START = re.compile(r'^\s*/\*[\*!]?')
# Строки внутри блока (* ...)
RE_BLOCK_INNER = re.compile(r'^\s*\*')
# Конец блока */
RE_BLOCK_END = re.compile(r'\*/')


def print_banner():
    print(f"\n{C.CYAN}{C.BOLD}")
    print("╔══════════════════════════════════════════════════════════╗")
    print("║        AI COMMENT REMOVER  •  Java Source Cleaner        ║")
    print("╚══════════════════════════════════════════════════════════╝")
    print(C.RESET)


def sep(w=62):
    print(f"{C.DIM}{'─'*w}{C.RESET}")


# ─── МОДЕЛЬ КОММЕНТАРИЯ ───────────────────────────────────────────────────────

class Comment:
    """Описывает один найденный комментарий."""
    STANDALONE = 'standalone'   # вся строка — комментарий
    INLINE     = 'inline'       # комментарий в конце строки с кодом
    BLOCK      = 'block'        # /** ... */ или /* ... */

    def __init__(self, kind, line_indices, display_lines,
                 inline_code_part=None):
        self.kind = kind
        # list of 0-based indices строк файла, которые относятся к этому комментарию
        self.line_indices = line_indices
        # Что показывать пользователю
        self.display_lines = display_lines
        # Для INLINE: часть строки без комментария (что оставить)
        self.inline_code_part = inline_code_part

    @property
    def line_number(self):
        return self.line_indices[0] + 1


# ─── ПАРСЕР ───────────────────────────────────────────────────────────────────

def find_comments(lines: list) -> list:
    """Возвращает список Comment для всех комментариев в файле."""
    results = []
    i = 0
    while i < len(lines):
        line = lines[i]

        # ── Javadoc / блочный комментарий ──
        if RE_BLOCK_START.match(line):
            block_indices = [i]
            block_display = [line]
            # Ищем закрывающий */
            if RE_BLOCK_END.search(line):
                # Однострочный блок /* ... */
                pass
            else:
                j = i + 1
                while j < len(lines):
                    block_indices.append(j)
                    block_display.append(lines[j])
                    if RE_BLOCK_END.search(lines[j]):
                        break
                    j += 1
                i = j  # перепрыгиваем конец блока

            results.append(Comment(
                kind=Comment.BLOCK,
                line_indices=block_indices,
                display_lines=block_display,
            ))
            i += 1
            continue

        # ── Standalone строчный комментарий ──
        m_standalone = RE_STANDALONE.match(line)
        if m_standalone:
            results.append(Comment(
                kind=Comment.STANDALONE,
                line_indices=[i],
                display_lines=[line],
            ))
            i += 1
            continue

        # ── Инлайн комментарий в конце строки ──
        # Важно: строки вида "// текст" уже пойманы выше как STANDALONE
        # Здесь ловим только "код(); // текст"
        m_inline = RE_INLINE.match(line)
        if m_inline:
            code_part = m_inline.group(1)
            comment_text = m_inline.group(2)
            results.append(Comment(
                kind=Comment.INLINE,
                line_indices=[i],
                display_lines=[line],
                inline_code_part=code_part,
            ))
            i += 1
            continue

        i += 1

    return results


# ─── ОТОБРАЖЕНИЕ ─────────────────────────────────────────────────────────────

def display_comment(filepath: str, comment: Comment, current: int, total: int):
    print(f"\n{C.BOLD}{C.YELLOW}[{current}/{total}]{C.RESET}  "
          f"{C.DIM}{Path(filepath).name}  строка {comment.line_number}{C.RESET}")
    sep()

    kind_label = {
        Comment.STANDALONE: f"{C.MAGENTA}standalone{C.RESET}",
        Comment.INLINE:     f"{C.CYAN}inline{C.RESET}",
        Comment.BLOCK:      f"{C.BLUE}javadoc/block{C.RESET}",
    }[comment.kind]
    print(f"  Тип: {kind_label}")
    print()

    for dl in comment.display_lines:
        print(f"  {C.RED}{C.BOLD}{dl.rstrip()}{C.RESET}")

    if comment.kind == Comment.INLINE:
        print()
        print(f"  {C.DIM}После удаления останется:{C.RESET}")
        print(f"  {C.GREEN}{comment.inline_code_part}{C.RESET}")

    print()
    sep()
    print(f"  {C.GREEN}[Y]{C.RESET} Удалить   "
          f"{C.CYAN}[T]{C.RESET} Оставить   "
          f"{C.MAGENTA}[S]{C.RESET} Пропустить файл   "
          f"{C.RED}[Q]{C.RESET} Выход")
    print()


def ask_user() -> str:
    while True:
        try:
            ans = input(f"  {C.BOLD}Твой выбор: {C.RESET}").strip().upper()
            if ans in ('Y', 'T', 'S', 'Q', ''):
                return ans or 'T'
            print(f"  {C.YELLOW}Нажми Y, T, S или Q{C.RESET}")
        except (KeyboardInterrupt, EOFError):
            print(f"\n{C.RED}Прервано.{C.RESET}")
            sys.exit(0)


# ─── ПРИМЕНЕНИЕ ИЗМЕНЕНИЙ ────────────────────────────────────────────────────

def apply_changes(filepath: str, decisions: list, lines: list):
    """
    decisions — список (Comment, action) где action='Y'|'T'
    lines     — оригинальные строки файла
    """
    shutil.copy2(filepath, filepath + '.bak')

    new_lines = list(lines)  # копия

    # Обрабатываем в обратном порядке, чтобы индексы не сдвигались
    to_delete = set()
    inline_replacements = {}  # idx -> новая строка

    for comment, action in decisions:
        if action != 'Y':
            continue
        if comment.kind == Comment.INLINE:
            # Оставляем код, убираем только комментарий
            idx = comment.line_indices[0]
            orig = lines[idx]
            # Восстанавливаем оригинальный отступ
            leading = re.match(r'^(\s*)', orig).group(1)
            inline_replacements[idx] = leading + comment.inline_code_part.strip() + '\n'
        else:
            for idx in comment.line_indices:
                to_delete.add(idx)

    # Применяем inline-замены
    for idx, new_line in inline_replacements.items():
        new_lines[idx] = new_line

    # Удаляем строки (фильтруем)
    result = [line for i, line in enumerate(new_lines) if i not in to_delete]

    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(result)

    return len(to_delete) + len(inline_replacements)


# ─── ОСНОВНОЙ ПОТОК ──────────────────────────────────────────────────────────

def process_directory(root_path: str):
    root = Path(root_path)
    if not root.exists():
        print(f"{C.RED}Путь не найден: {root_path}{C.RESET}")
        sys.exit(1)

    java_files = list(root.rglob('*.java'))
    if not java_files:
        print(f"{C.YELLOW}Java-файлы не найдены в: {root_path}{C.RESET}")
        sys.exit(0)

    print(f"{C.CYAN}Java-файлов найдено: {C.BOLD}{len(java_files)}{C.RESET}")
    print(f"{C.DIM}Сканируем...{C.RESET}\n")

    # Первый проход: собираем все комментарии
    all_findings = []  # (filepath, Comment, lines_of_file)
    for jf in java_files:
        with open(str(jf), 'r', encoding='utf-8', errors='replace') as f:
            lines = f.readlines()
        for c in find_comments(lines):
            all_findings.append((str(jf), c, lines))

    if not all_findings:
        print(f"{C.GREEN}Комментарии не найдены. Код чист!{C.RESET}")
        sys.exit(0)

    total = len(all_findings)
    print(f"{C.YELLOW}{C.BOLD}Найдено комментариев: {total}{C.RESET}\n")
    input(f"{C.DIM}Enter чтобы начать просмотр...{C.RESET}")

    # Второй проход: интерактивный
    # decisions_by_file: filepath -> list of (Comment, action, lines)
    from collections import defaultdict
    decisions_by_file = defaultdict(list)
    files_lines = {}       # filepath -> lines (кешируем)
    skipped_files = set()

    stats = {'deleted': 0, 'kept': 0, 'skipped': 0}

    for idx, (filepath, comment, lines) in enumerate(all_findings, 1):
        if filepath in skipped_files:
            stats['skipped'] += 1
            continue

        files_lines[filepath] = lines
        display_comment(filepath, comment, idx, total)
        ans = ask_user()

        if ans == 'Y':
            decisions_by_file[filepath].append((comment, 'Y'))
            print(f"  {C.RED}✓ Помечено на удаление{C.RESET}")
            stats['deleted'] += 1
        elif ans == 'T':
            print(f"  {C.CYAN}→ Оставлено{C.RESET}")
            stats['kept'] += 1
        elif ans == 'S':
            skipped_files.add(filepath)
            print(f"  {C.MAGENTA}⊘ Файл пропущен{C.RESET}")
            stats['skipped'] += 1
        elif ans == 'Q':
            print(f"\n{C.YELLOW}Выход без изменений.{C.RESET}")
            sys.exit(0)

    # Применяем
    if not decisions_by_file:
        print(f"\n{C.CYAN}Ничего не помечено. Файлы не изменены.{C.RESET}")
        print_stats(stats)
        return

    print(f"\n{C.BOLD}{C.YELLOW}═══ ПРИМЕНЕНИЕ ИЗМЕНЕНИЙ ═══{C.RESET}\n")
    total_lines_removed = 0

    for filepath, dec_list in decisions_by_file.items():
        lines = files_lines[filepath]
        n = apply_changes(filepath, dec_list, lines)
        total_lines_removed += n
        print(f"  {C.GREEN}✓{C.RESET} {Path(filepath).name}  "
              f"{C.DIM}(изменено строк: {n}){C.RESET}")

    print(f"\n{C.DIM}Бэкапы сохранены как .bak рядом с оригиналами.{C.RESET}")
    print_stats(stats, total_lines_removed)


def print_stats(stats, removed_lines=0):
    print(f"\n{C.CYAN}{C.BOLD}╔══════════ ИТОГ ══════════╗{C.RESET}")
    print(f"{C.CYAN}  Удалено:   {C.RED}{C.BOLD}{stats['deleted']:>4}{C.RESET}{C.CYAN} комментариев{C.RESET}")
    print(f"{C.CYAN}  Оставлено: {C.GREEN}{C.BOLD}{stats['kept']:>4}{C.RESET}{C.CYAN} комментариев{C.RESET}")
    print(f"{C.CYAN}  Пропущено: {C.MAGENTA}{C.BOLD}{stats['skipped']:>4}{C.RESET}{C.CYAN} комментариев{C.RESET}")
    if removed_lines:
        print(f"{C.CYAN}  Строк изменено в файлах: {C.YELLOW}{C.BOLD}{removed_lines}{C.RESET}")
    print(f"{C.CYAN}{C.BOLD}╚══════════════════════════╝{C.RESET}\n")


# ─── УДАЛЕНИЕ .bak ФАЙЛОВ ────────────────────────────────────────────────────

def delete_bak_files(root_path: str):
    root = Path(root_path)
    bak_files = list(root.rglob('*.bak'))

    if not bak_files:
        print(f"{C.YELLOW}Файлы .bak не найдены в: {root_path}{C.RESET}")
        return

    print(f"{C.CYAN}Найдено .bak файлов: {C.BOLD}{len(bak_files)}{C.RESET}\n")
    for bf in bak_files:
        print(f"  {C.DIM}{bf}{C.RESET}")

    print()
    ans = input(f"  {C.BOLD}{C.RED}Удалить все {len(bak_files)} файлов? [Y/N]: {C.RESET}").strip().upper()
    if ans == 'Y':
        for bf in bak_files:
            bf.unlink()
        print(f"\n  {C.GREEN}✓ Удалено {len(bak_files)} .bak файлов{C.RESET}")
    else:
        print(f"\n  {C.CYAN}Отменено.{C.RESET}")

# ─── ТОЧКА ВХОДА ─────────────────────────────────────────────────────────────
if __name__ == '__main__':
    print_banner()

    default_path = r'C:\Users\Кирилл\Desktop\$$$$$\cheat\cookie\src\main\java'

    # Режим удаления .bak: python скрипт.py --cleanbak [путь]
    if '--cleanbak' in sys.argv:
        sys.argv.remove('--cleanbak')
        target_path = sys.argv[1] if len(sys.argv) > 1 else default_path
        print(f"{C.YELLOW}{C.BOLD}Режим удаления .bak файлов{C.RESET}")
        print(f"{C.DIM}Путь: {target_path}{C.RESET}\n")
        delete_bak_files(target_path)
        sys.exit(0)

    if len(sys.argv) > 1:
        target_path = sys.argv[1]
    else:
        target_path = default_path
        print(f"{C.DIM}Дефолтный путь:{C.RESET}")
        print(f"  {C.CYAN}{target_path}{C.RESET}\n")
        print(f"  {C.DIM}Для удаления .bak файлов запусти с флагом --cleanbak{C.RESET}\n")
        custom = input(f"{C.DIM}Enter — продолжить, или введи другой путь: {C.RESET}").strip()
        if custom:
            target_path = custom

    process_directory(target_path)