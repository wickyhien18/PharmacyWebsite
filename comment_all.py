import os
import re

def to_words(camel_case_str):
    return re.sub('([a-z])([A-Z])', r'\1 \2', camel_case_str).lower()

def generate_method_comment(method_name, return_type, params):
    desc = to_words(method_name)
    desc = desc.capitalize()
    
    # Improve descriptions based on common prefixes
    if method_name.startswith('get'): desc = "Retrieves " + desc[4:]
    elif method_name.startswith('set'): desc = "Sets the " + desc[4:]
    elif method_name.startswith('is'): desc = "Checks if " + desc[3:]
    elif method_name.startswith('find'): desc = "Finds " + desc[5:]
    elif method_name.startswith('create') or method_name.startswith('insert') or method_name.startswith('add'): desc = "Creates a new " + desc.split(' ', 1)[-1]
    elif method_name.startswith('update'): desc = "Updates an existing " + desc[7:]
    elif method_name.startswith('delete') or method_name.startswith('remove'): desc = "Deletes " + desc[7:]
    
    comment = f"    /**\n     * {desc}.\n     *\n"
    
    has_params = False
    if params.strip():
        param_list = params.split(',')
        for p in param_list:
            parts = p.strip().split()
            if len(parts) >= 2:
                # ignore annotations
                p_name = parts[-1]
                if not p_name.startswith('@'):
                    comment += f"     * @param {p_name} the {p_name}\n"
                    has_params = True
    
    if return_type and return_type != 'void':
        comment += f"     * @return the {return_type} result\n"
        has_params = True
        
    if not has_params:
        comment = f"    /**\n     * {desc}.\n"
        
    comment += "     */\n"
    return comment

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
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
            
        if not in_comment:
            # Check class/interface
            class_match = re.search(r'public (class|interface|record|enum) (\w+)', line)
            if class_match and not "class class " in line:
                name = class_match.group(2)
                type_name = class_match.group(1)
                
                has_comment = False
                for j in range(max(0, i-8), i):
                    if '*/' in lines[j] or '//' in lines[j]:
                        has_comment = True
                
                if not has_comment:
                    out_lines.append(f"/**\n * {type_name.capitalize()} {name}.\n * Provides functionality and data modeling for {name}.\n */\n")
            
            # Check method
            method_match = re.search(r'^\s*(public|protected|private)\s+([\w<>\.\[\]\?]+)\s+(\w+)\s*\((.*?)\)', line)
            if method_match:
                access = method_match.group(1)
                return_type = method_match.group(2)
                name = method_match.group(3)
                params = method_match.group(4)
                
                if return_type not in ['class', 'interface', 'record', 'enum'] and return_type != name: # exclude constructors
                    # check for existing comments
                    has_comment = False
                    for j in range(max(0, i-8), i):
                        if '*/' in lines[j]:
                            has_comment = True
                    
                    if not has_comment:
                        out_lines.append(generate_method_comment(name, return_type, params))
            
            # Check constructor
            constructor_match = re.search(r'^\s*public\s+(\w+)\s*\((.*?)\)\s*\{?', line)
            if constructor_match:
                name = constructor_match.group(1)
                params = constructor_match.group(2)
                # check if it matches class name, but we don't have class name here reliably
                # a simple heuristic: if it has no return type and is capitalized
                if name[0].isupper() and not "class" in line:
                    has_comment = False
                    for j in range(max(0, i-6), i):
                        if '*/' in lines[j]:
                            has_comment = True
                    if not has_comment:
                        out_lines.append(generate_method_comment(name, None, params))


        out_lines.append(line)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(out_lines)

for root, dirs, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            try:
                process_file(os.path.join(root, f))
            except Exception as e:
                print(f"Error processing {f}: {e}")
print("Finished commenting all files.")
