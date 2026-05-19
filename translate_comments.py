import os
import re
# pyrefly: ignore [missing-import]
from deep_translator import GoogleTranslator
import time

translator = GoogleTranslator(source='vi', target='en')

def is_vietnamese(text):
    return any(ord(c) > 127 for c in text)

def translate_safe(text):
    if not text.strip(): return text
    try:
        # Avoid hitting API limits too hard
        time.sleep(0.1)
        res = translator.translate(text)
        return res if res else text
    except Exception as e:
        print(f"Failed to translate: {text}. Error: {e}")
        return text

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    new_lines = []
    
    for i, line in enumerate(lines):
        original_line = line
        
        if not is_vietnamese(line):
            new_lines.append(line)
            continue
            
        # Handle inline comments: //
        if '//' in line:
            parts = line.split('//', 1)
            before = parts[0]
            after = parts[1]
            if is_vietnamese(after):
                translated = translate_safe(after)
                print(f"[{os.path.basename(filepath)}] // {after.strip()} -> // {translated.strip()}")
                # Maintain original newline
                ending = '\n' if line.endswith('\n') else ''
                line = f"{before}// {translated}{ending}"
        
        # Handle block comment lines: * or /*
        elif re.match(r'^\s*(?:/\*\*?|\*)\s*', line) and is_vietnamese(line):
            match = re.match(r'^(\s*(?:/\*\*?|\*)\s*)(.*?)(\s*\*/)?\s*$', line)
            if match:
                prefix = match.group(1) or ""
                text = match.group(2) or ""
                suffix = match.group(3) or ""
                
                if is_vietnamese(text):
                    translated = translate_safe(text)
                    print(f"[{os.path.basename(filepath)}] * {text.strip()} -> * {translated.strip()}")
                    ending = '\n' if line.endswith('\n') else ''
                    line = f"{prefix}{translated}{suffix}{ending}"
        
        # Handle string literals containing Vietnamese (like Exception messages)
        elif '"' in line and is_vietnamese(line):
            # Find all string literals
            import ast
            # A simple regex to find content between quotes
            matches = re.finditer(r'"([^"]*)"', line)
            for m in matches:
                orig = m.group(1)
                if is_vietnamese(orig):
                    translated = translate_safe(orig)
                    print(f"[{os.path.basename(filepath)}] \"{orig}\" -> \"{translated}\"")
                    line = line.replace(f'"{orig}"', f'"{translated}"')

        new_lines.append(line)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

for root, dirs, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            process_file(os.path.join(root, f))
print("Finished translating comments and strings to English.")
