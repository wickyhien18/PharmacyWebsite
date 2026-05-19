import os
import re

directories = [
    'src/main/java/Pharmacy/DTO',
    'src/main/java/Pharmacy/Entities',
    'src/main/java/Pharmacy/Repositories'
]

def process_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    out_lines = []
    in_comment = False
    
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('/*'):
            in_comment = True
        if in_comment and '*/' in stripped:
            in_comment = False
            out_lines.append(line)
            continue
            
        if not in_comment and ('public class ' in line or 'public interface ' in line or 'public record ' in line):
            match = re.search(r'public (class|interface|record) (\w+)', line)
            if match:
                name = match.group(2)
                type_name = match.group(1)
                if "Entities" in filepath:
                    desc = "Database Entity"
                elif "DTO" in filepath:
                    desc = "Data Transfer Object"
                else:
                    desc = "Repository interface"
                
                comment = f"/**\n * {desc} for {name}.\n * This class is used to map data and handle basic structure.\n */\n"
                
                has_comment = False
                for j in range(max(0, i-5), i):
                    if '*/' in lines[j]:
                        has_comment = True
                
                if not has_comment:
                    out_lines.append(comment)
        
        out_lines.append(line)
        
    with open(filepath, 'w') as f:
        f.writelines(out_lines)

for d in directories:
    if os.path.exists(d):
        for root, _, files in os.walk(d):
            for f in files:
                if f.endswith('.java'):
                    process_file(os.path.join(root, f))
print("Added basic comments to DTO, Entities, and Repositories.")
